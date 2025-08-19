package com.example.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor

public enum ErrorCode {

	// -------------------- Global --------------------
	INTERNAL_SERVER_ERROR(500, "GLOBAL-001", "서버에 오류가 발생하였습니다."),
	INVALID_INPUT_VALUE(400, "GLOBAL-002", "잘못된 입력 값입니다."),
	METHOD_NOT_ALLOWED(405, "GLOBAL-003", "허용되지 않은 HTTP method입니다."),
	UNSUPPORTED_MEDIA_TYPE(415, "GLOBAL-004", "지원하지 않는 미디어 타입입니다."),
	HANDLE_ACCESS_DENIED(403, "GLOBAL-005", "접근 권한이 없습니다."),
	RESOURCE_NOT_FOUND(404, "GLOBAL-006", "요청한 리소스를 찾을 수 없습니다."),

	// -------------------- Vehicle --------------------
	VEHICLE_NOT_FOUND(404, "VEHICLE-001", "해당 차량을 찾을 수 없습니다."),
	VEHICLE_ALREADY_EXISTS(409, "VEHICLE-002", "이미 등록된 차량입니다."),
	INVALID_VEHICLE_STATUS(400, "VEHICLE-003", "차량 상태가 올바르지 않습니다."),
	INVALID_VEHICLE_NUMBER(400, "VEHICLE-004", "차량 번호 형식이 올바르지 않습니다. '하/허/호' 형식으로 입력하세요."),
	// -------------------- Record --------------------
	RECORD_NOT_FOUND(404, "RECORD-001", "운행 기록이 존재하지 않습니다."),
	INVALID_RECORD_DURATION(400, "RECORD-002", "운행 시간 정보가 유효하지 않습니다."),

	// -------------------- GPS Record --------------------
	GPS_RECORD_NOT_FOUND(404, "GPS-001", "GPS 기록이 존재하지 않습니다."),
	INVALID_GPS_COORDINATE(400, "GPS-002", "GPS 좌표 값이 유효하지 않습니다."),
	EMPTY_CLIST_ERROR(500, "GPS-003", "CLIST가 비어있습니다."),
	GPS_RECORD_TOO_RECENT(404, "GPS-004", "2분 전 기준 GPS 기록이 존재하지 않습니다."),

	// -------------------- Location --------------------
	LOCATION_NOT_FOUND(404, "LOCATION-001", "위치 정보를 찾을 수 없습니다."),
	LOCATION_SERVICE_ERROR(500, "LOCATION-002", "외부 위치 서비스 호출 중 오류가 발생했습니다."),

	INVALID_TOKEN_ERROR(500, "TOKEN-001", "유효하지 않은 토큰입니다."),
	JWT_SECRET_KEY_MISSING(500, "TOKEN-002", "JWT Secret 키를 찾지 못했습니다."),

	// ===== 회원가입 / 로그인 관련 =====
	DUPLICATE_ID(409, "AUTH-001", "이미 사용 중인 ID입니다."),
	INVALID_ID_OR_PASSWORD(401, "AUTH-002", "아이디 또는 비밀번호가 올바르지 않습니다."),
	INVALID_ROLE(400, "AUTH-003", "잘못된 역할(role)이 지정되었습니다."),

	ONOFF_PRODUCER_ERROR(500, "KAFKA-001", "Kafka에서 OnOff message를 보내는데 실패했습니다."),
	GPS_PRODUCER_ERROR(500, "KAFKA-002", "Kafka에서 주기정보 message를 보내는데 실패했습니다.")
	;

	// 예시 에러코드
	// INPUT_INVALID_VALUE_ERROR(400, "GLOBAL-002", "잘못된 입력 값입니다."),
	// EMPTY_INPUT_ERROR(400, "GLOBAL-003", "입력 값이 비어있습니다."),
	// POST_TIME_ERROR(400, "GLOBAL-004", "시간이 지났습니다."),
	// INPUT_INVALID_TYPE_ERROR(400, "GLOBAL-003", "잘못된 입력 타입입니다."),
	// REQUEST_PARAMETER_NOT_FOUND_ERROR(400, "GLOBAL-004", "입력 파라미터가 존재하지 않습니다."),
	// REQUEST_PARAMETER_TYPE_NOT_MATCH_ERROR(400, "GLOBAL-005", "입력 파라미터의 타입이 올바르지 않습니다."),
	//volunteerActivity
	// NOT_FOUND_VOLUNTEER_ACTIVITY_ERROR(404, "VOLUNTEER-001", "봉사활동을 찾을 수 없습니다."),
	// UNAVAILABLE_CANCLE_VOLUNTEER_ACTIVITY_ERROR(400, "VOLUNTEER-002", "봉사활동 취소가 불가능합니다."),
	// UNAVAILABLE_COMPLETE_VOLUNTEER_ACTIVITY_ERROR(400, "VOLUNTEER-003", "봉사활동 완료가 불가능합니다."),
	// ALREADY_REGISTERED_ERROR(400, "VOLUNTEER-004", "해당 일에 이미 신청한 봉사활동이 있습니다."),
	// ALREADY_MATCHING_ERROR(400, "VOLUNTEER-005", "이미 매칭된 봉사활동입니다."),
	//review
	// NOT_FOUND_REVIEW_ERROR(404, "REVIEW-001", "리뷰를 찾을 수 없습니다."),
	// REVIEW_ACCESS_DENIED_ERROR(403, "REVIEW-002", "리뷰에 접근 권한이 없습니다."),
	// UNABLE_TO_WRITE_REVIEW_ERROR(400, "REVIEW-003", "리뷰 작성이 불가능합니다."),
	//item
	// CHAT_ROOM_JOIN_ERROR(400,"CHATROOM-001", "?"),
	// ITEM_NOT_FOUND_ERROR(404,"ITEM-001", "해당 상품이 존재하지 않습니다"),
	// PRODUCT_ACCESS_DENIED_ERROR(403,"ITEM-002", "해당 상품에 접근 권한이 없습니다"),
	// CHAT_ROOM_LIMITED_USER_ERROR(400,"CHATROOM-003", "방에 인원이 다 찼습니다"),
	//streamRoom
	// STREAM_ROOM_JOIN_ERROR(400,"STREAMROOM-001", "?"),
	// STREAM_ROOM_NOT_FOUND_ERROR(404,"STREAMROOM-002", "방을 찾을 수 없습니다"),
	//order
	// INSUFFICIENT_USER_POINTS(400, "ORDERS-001", "유저 포인트가 부족합니다."),
	// NOT_FOUND_ORDER_ERROR(404, "ORDERS-002", "주문이 존재하지 않습니다."),

	//jwt
	// INVALID_TOKEN_ERROR(401, "AUTH-001", "jwt 토큰이 유효하지 않습니다."),
	// EXPIRED_TOKEN_ERROR(401, "AUTH-002", "jwt 토큰이 만료되었습니다."),
	// NULL_TOKEN_ERROR(401, "AUTH-003", "jwt 토큰이 존재하지 않습니다."),
	// TOKEN_CATEGORY_NOT_MATCHED_ERROR(401, "AUTH-004", "토큰의 카테고리가 일치하지 않습니다."),
	// ALREADY_RE_ISSUED_TOKEN_ERROR(400, "AUTH-005", "이미 액세스 토큰 재발급에 사용된 리프레시 토큰입니다."),
	// REFRESH_TOKEN_NOT_FOUND_ERROR(404, "AUTH-006", "리프레시 토큰을 찾을 수 없습니다."),
	// REFRESH_TOKEN_EXPIRED_ERROR(401, "AUTH-007", "리프레시 토큰이 만료되었습니다."),
	// REFRESH_TOKEN_DB_NOT_FOUND_ERROR(404, "AUTH-008", "리프레시 토큰을 DB에서 찾을 수 없습니다."),

	//auth
	// CREDENTIALS_NOT_MATCHED_ERROR(401, "AUTH-001", "userName 또는 password가 일치하지 않습니다."),
	// ALREADY_EXIST_USER_ERROR(400, "AUTH-002", "이미 존재하는 사용자입니다."),
	// USERNAME_NOT_FOUND_ERROR(404, "AUTH-003", "사용자 이름을 찾을 수 없습니다."),
	// NOT_FOUND_USER_ERROR(404, "AUTH-004", "사용자를 찾을 수 없습니다."),
	// NOT_FOUND_DESIGNER_ERROR(404, "AUTH-005", "디자이너를 찾을 수 없습니다."),
	// ACCESS_DENIED_ERROR(403, "AUTH-005", "접근 권한이 없습니다."),
	// ALREADY_LOGOUT_ERROR(400, "AUTH-006", "이미 로그아웃 되었습니다."),
	// PRINCIPAL_NOT_FOUND_ERROR(404, "AUTH-007", "principal을 찾을 수 없습니다."),
	// INVALID_USER_TYPE_ERROR(400, "AUTH-008", "잘못된 userType 입니다."),
	// USERNAME_NOT_FOUND_ERROR(404, "AUTH-002", "사용자 이름을 찾을 수 엄습니다.");
	// AUTHENTICATION_ERROR(401, "AUTH-003", "인증에 실패했습니다. 인증 수단이 유효한지 확인하세요."),
	// AUTHORIZATION_ERROR(403, "AUTH-004", "권한이 존재하지 않습니다."),
	//
	//aws
	// AWS_S3_IMAGE_NOT_FOUND_ERROR(404, "AWS-001", "이미지를 찾을 수 없습니다."),
	// AWS_S3_IMAGE_EXTEND_ERROR(400, "AWS-002", "허용되지 않는 확장자입니다."),
	//
	// message
	// TOO_MANY_REQUEST_ERROR(429, "MESSAGE-001", "너무 많은 인증 메시지를 요청했습니다. 24시간 후 요청해주세요."),
	//
	// CERTIFICATION_NUMBER_NOT_FOUND_ERROR(404, "MESSAGE-001", "인증번호가 만료되었거나 존재하지 않습니다."),
	// CERTIFICATION_NUMBER_NOT_MATCH_ERROR(400, "MESSAGE-002", "인증 번호가 일치하지 않습니다."),
	// INVALID_PHONE_NUMBER_FORMAT(400, "MESSAGE-003", "올바른 형식의 전화번호가 아닙니다. 예: +821012345678");
	//
	// //ouath
	// OAUTH2_NOT_AUTHENTICATED_ERROR(401, "AUTH-004", "OAuth2 인증에 실패하였습니다."),
	//
	// //외부 API
	// EXTERNAL_API_ERROR(500, "EXTERNAL-001", "외부 api 호출에 실패했습니다. 잠시 후 요청해주세요."),
	//
	// //member
	// LOGIN_ON_NOT_VERIFIED_ERROR(400, "MEMBER-001", "인증되지 않은 사용자로 로그인을 시도했습니다."),
	//
	// ALREADY_VERIFIED_ERROR(400, "MEMBER-002", "이미 인증된 사용자입니다."),
	//
	// MEMBER_NOT_FOUND_ERROR(404, "MEMBER-003", "사용자 데이터를 찾지 못하였습니다."),
	//
	// //geo
	// GEO_TRANSFORMED_ERROR(400, "GEO-001", "좌표 변환에 실패했습니다. 입력이 유효한지 확인해주세요"),
	//
	// //capsule skin
	// CAPSULE_SKIN_NOT_FOUND_ERROR(404, "SKIN-001", "캡슐 스킨을 찾지 못하였습니다."),
	//
	// //capsule
	// CAPSULE_NOT_FOUND_ERROR(404, "CAPSULE-001", "캡슐을 찾지 못하였습니다."),
	// NO_CAPSULE_AUTHORITY_ERROR(403, "CAPSULE-002", "캡슐에 접근 권한이 없습니다."),
	//
	// //friend
	// FRIEND_NOT_FOUND_ERROR(404, "FRIEND-001", "친구를 찾지 못하였습니다"),
	// FRIEND_DUPLICATE_ID_ERROR(404, "FRIEND-002", "친구 아이디가 중복되었습니다."),
	//
	// //group
	// GROUP_CREATE_ERROR(400, "GROUP-001", "그룹 생성에 실패하였습니다."),
	// GROUP_NOT_FOUND_ERROR(404, "GROUP-002", "그룹을 찾을 수 없습니다"),
	//
	// //friend invite
	// FRIEND_INVITE_NOT_FOUND_ERROR(404, "FRIEND-INVITE-001", "친구 요청을 찾지 못하였습니다."),
	// FRIEND_TWO_WAY_INVITE_ERROR(400, "FRIEND-INVITE-002", "친구 요청을 받은 상태입니다.");


	private final int status;
	private final String code;
	private final String message;
}
