# Route 53 Hosted Zone 정보 가져오기
data "aws_route53_zone" "main" {
  name = var.domain_name
}

# ACM 인증서 생성 (CloudFront용, us-east-1)
resource "aws_acm_certificate" "main" {
  provider = aws.us_east_1

  domain_name       = var.domain_name
  subject_alternative_names = ["*.${var.domain_name}"]
  validation_method = "DNS"

  tags = {
    Name = "${var.domain_name}-certificate"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ACM 인증서 생성 (ALB용, ap-northeast-2)
resource "aws_acm_certificate" "alb" {
  domain_name       = "api.${var.domain_name}"
  validation_method = "DNS"

  tags = {
    Name = "api.${var.domain_name}-certificate"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ACM 인증서 DNS 검증을 위한 Route 53 레코드 생성 (CloudFront + ALB)
resource "aws_route53_record" "cert_validation" {
  for_each = merge(
    {
      for dvo in aws_acm_certificate.main.domain_validation_options : dvo.domain_name => dvo
    },
    {
      for dvo in aws_acm_certificate.alb.domain_validation_options : dvo.domain_name => dvo
    }
  )

  allow_overwrite = true
  name            = each.value.resource_record_name
  records         = [each.value.resource_record_value]
  ttl             = 60
  type            = each.value.resource_record_type
  zone_id         = data.aws_route53_zone.main.zone_id
}

# ACM 인증서 검증 완료 대기 (CloudFront용)
resource "aws_acm_certificate_validation" "main" {
  provider = aws.us_east_1

  certificate_arn         = aws_acm_certificate.main.arn
  validation_record_fqdns = [for dvo in aws_acm_certificate.main.domain_validation_options : aws_route53_record.cert_validation[dvo.domain_name].fqdn]
}

# ACM 인증서 검증 완료 대기 (ALB용)
resource "aws_acm_certificate_validation" "alb" {
  certificate_arn         = aws_acm_certificate.alb.arn
  validation_record_fqdns = [for dvo in aws_acm_certificate.alb.domain_validation_options : aws_route53_record.cert_validation[dvo.domain_name].fqdn]
}

# S3 정적 사이트를 위한 CloudFront 배포
resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name = aws_s3_bucket_website_configuration.my_bucket.website_endpoint
    origin_id   = "S3-${var.domain_name}"

    custom_origin_config {
      origin_protocol_policy = "http-only"
      http_port              = 80
      https_port             = 443
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  enabled             = true
  is_ipv6_enabled     = true
  comment             = "S3 distribution for ${var.domain_name}"
  default_root_object = "index.html"

  aliases = [var.domain_name]

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD", "OPTIONS"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-${var.domain_name}"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  price_class = "PriceClass_200" # Asia

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = aws_acm_certificate_validation.main.certificate_arn
    ssl_support_method  = "sni-only"
  }
}

# Route 53 레코드: 루트 도메인 -> CloudFront (프론트엔드)
resource "aws_route53_record" "frontend" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_cloudfront_distribution.s3_distribution.domain_name
    zone_id                = aws_cloudfront_distribution.s3_distribution.hosted_zone_id
    evaluate_target_health = false
  }
}

# Route 53 레코드: API 서브도메인 -> ALB (백엔드)
resource "aws_route53_record" "backend" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "api"
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = true
  }
}