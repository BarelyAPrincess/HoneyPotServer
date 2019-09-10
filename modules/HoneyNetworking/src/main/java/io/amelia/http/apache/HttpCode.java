/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.apache;

import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpCode
{
	// Reference: http://en.wikipedia.org/wiki/List_of_HTTP_status_codes

	public static final int HTTP_CONTINUE = 100;
	public static final int HTTP_SWITCHING_PROTOCOLS = 101;
	public static final int HTTP_PROCESSING = 102;
	public static final int HTTP_OK = 200;
	public static final int HTTP_CREATED = 201;
	public static final int HTTP_ACCEPTED = 202;
	public static final int HTTP_NOT_AUTHORITATIVE = 203;
	public static final int HTTP_NO_CONTENT = 204;
	public static final int HTTP_RESET = 205;
	public static final int HTTP_PARTIAL = 206;
	public static final int HTTP_MULTI_STATUS = 207;
	public static final int HTTP_ALREADY_REPORTED = 208;
	public static final int HTTP_IM_USED = 226;
	public static final int HTTP_MULT_CHOICE = 300;
	public static final int HTTP_MOVED_PERM = 301;
	public static final int HTTP_MOVED_TEMP = 302;
	public static final int HTTP_SEE_OTHER = 303;
	public static final int HTTP_NOT_MODIFIED = 304;
	public static final int HTTP_USE_PROXY = 305;
	public static final int HTTP_TEMPORARY_REDIRECT = 307;
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	public static final int HTTP_PAYMENT_REQUIRED = 402;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_BAD_METHOD = 405;
	public static final int HTTP_NOT_ACCEPTABLE = 406;
	public static final int HTTP_PROXY_AUTH = 407;
	public static final int HTTP_CLIENT_TIMEOUT = 408;
	public static final int HTTP_CONFLICT = 409;
	public static final int HTTP_GONE = 410;
	public static final int HTTP_LENGTH_REQUIRED = 411;
	public static final int HTTP_PRECON_FAILED = 412;
	public static final int HTTP_ENTITY_TOO_LARGE = 413;
	public static final int HTTP_REQ_TOO_LONG = 414;
	public static final int HTTP_UNSUPPORTED_TYPE = 415;
	public static final int HTTP_RANGE_NOT_SATISFIABLE = 416;
	public static final int HTTP_EXPECTATION_FAILED = 417;
	public static final int HTTP_TEA_POT = 418;
	public static final int HTTP_THE_DOCTOR = 418;
	public static final int HTTP_BLUE_BOX = 418;
	public static final int HTTP_INSUFFICIENT_STORAGE_ON_RESOURCE = 419;
	public static final int HTTP_METHOD_FAILURE = 420;
	public static final int HTTP_DESTINATION_LOCKED = 421;
	public static final int HTTP_UNPROCESSABLE_ENTITY = 422;
	public static final int HTTP_LOCKED = 423;
	public static final int HTTP_FAILED_DEPENDENCY = 424;
	public static final int HTTP_UPGRADE_REQUIRED = 426;
	public static final int HTTP_TOO_MANY_REQUESTS = 429;
	public static final int HTTP_UNAVAILABLE_FOR_LEGAL_REASONS = 451;
	public static final int HTTP_INTERNAL_ERROR = 500;
	public static final int HTTP_NOT_IMPLEMENTED = 501;
	public static final int HTTP_BAD_GATEWAY = 502;
	public static final int HTTP_UNAVAILABLE = 503;
	public static final int HTTP_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_VERSION = 505;
	public static final int HTTP_VARIANT_ALSO_NEGOTIATES = 506;
	public static final int HTTP_INSUFFICIENT_STORAGE = 507;
	public static final int HTTP_LOOP_DETECTED = 508;
	public static final int HTTP_NOT_EXTENDED = 510;

	public static String msg( int code )
	{
		switch ( code )
		{
			case HTTP_OK:
				return "OK";
			case HTTP_CONTINUE:
				return "Continue";
			case HTTP_CREATED:
				return "Created";
			case HTTP_ACCEPTED:
				return "Accepted";
			case HTTP_NOT_AUTHORITATIVE:
				return "Non-Authoritative Information";
			case HTTP_NO_CONTENT:
				return "No Content";
			case HTTP_RESET:
				return "Reset Content";
			case HTTP_PARTIAL:
				return "Partial Content";
			case HTTP_MULT_CHOICE:
				return "Multiple Choices";
			case HTTP_MOVED_PERM:
				return "Moved Permanently";
			case HTTP_MOVED_TEMP:
				return "Temporary Redirect";
			case HTTP_SEE_OTHER:
				return "See Other";
			case HTTP_NOT_MODIFIED:
				return "Not Modified";
			case HTTP_USE_PROXY:
				return "Use Proxy";
			case HTTP_TEMPORARY_REDIRECT:
				return "Temporary Redirect";
			case HTTP_BAD_REQUEST:
				return "Bad Request";
			case HTTP_UNAUTHORIZED:
				return "Unauthorized";
			case HTTP_PAYMENT_REQUIRED:
				return "Payment Required";
			case HTTP_FORBIDDEN:
				return "Forbidden";
			case HTTP_NOT_FOUND:
				return "Not Found";
			case HTTP_BAD_METHOD:
				return "Method Not Allowed";
			case HTTP_NOT_ACCEPTABLE:
				return "Not Acceptable";
			case HTTP_PROXY_AUTH:
				return "Proxy Authentication Required";
			case HTTP_CLIENT_TIMEOUT:
				return "Request Time-Out";
			case HTTP_CONFLICT:
				return "Conflict";
			case HTTP_GONE:
				return "Gone";
			case HTTP_LENGTH_REQUIRED:
				return "Length Required";
			case HTTP_PRECON_FAILED:
				return "Precondition Failed";
			case HTTP_ENTITY_TOO_LARGE:
				return "Request Entity Too Large";
			case HTTP_REQ_TOO_LONG:
				return "Request-URI Too Large";
			case HTTP_UNSUPPORTED_TYPE:
				return "Unsupported Media Type";
			case HTTP_TOO_MANY_REQUESTS:
				return "Too Many Requests";
			case HTTP_UNAVAILABLE_FOR_LEGAL_REASONS:
				return "Unavailable for Legal Reasons";
			case HTTP_INTERNAL_ERROR:
				return "Internal Server Error";
			case HTTP_NOT_IMPLEMENTED:
				return "Not Implemented";
			case HTTP_BAD_GATEWAY:
				return "Bad Gateway";
			case HTTP_UNAVAILABLE:
				return "Service Unavailable";
			case HTTP_GATEWAY_TIMEOUT:
				return "Gateway Timeout";
			case HTTP_VERSION:
				return "HTTP Version Not Supported";
				// case HTTP_THE_DOCTOR:
				// return "I'm a Madman With A Blue Box!";
			case HTTP_BLUE_BOX:
				return "Time and Relative Dimensions in Space. Yes, that's it. Names are funny. It's me. I'm the TARDIS.";
			default:
				return HttpResponseStatus.valueOf( code ).reasonPhrase().toString();
		}
	}
}
