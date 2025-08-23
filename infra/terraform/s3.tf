resource "aws_s3_bucket" "my_bucket" {
  bucket = var.s3_bucket_name

  tags = {
    Name        = "GPS Tracker Frontend Bucket"
    Environment = "Dev"
  }

  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "my_bucket" {
  bucket = aws_s3_bucket.my_bucket.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_website_configuration" "my_bucket" {
  bucket = aws_s3_bucket.my_bucket.id

  index_document {
    suffix = "index.html"
  }
  error_document {
    key = "index.html"
  }
}

resource "aws_s3_bucket_policy" "my_bucket_policy" {
  bucket = aws_s3_bucket.my_bucket.id

  policy = data.aws_iam_policy_document.my_bucket_policy_doc.json
}

data "aws_iam_policy_document" "my_bucket_policy_doc" {
    version = "2012-10-17"
  statement {
    sid = "AllowPublicRead"
    effect = "Allow"
    actions = [
      "s3:GetObject"
    ]
    resources = [
      "${aws_s3_bucket.my_bucket.arn}/*"
    ]
    principals {
        type        = "AWS"
        identifiers = ["*"]
    }
  }
}