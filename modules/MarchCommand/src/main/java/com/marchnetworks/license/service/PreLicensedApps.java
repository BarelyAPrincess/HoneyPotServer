package com.marchnetworks.license.service;

import com.marchnetworks.command.common.CommonAppUtils;

import java.util.HashMap;
import java.util.Map;

public class PreLicensedApps
{
	private static final Map<String, byte[]> licenses = new HashMap();

	static
	{
		String appId = AppIds.COMMAND_CLIENT.getAppId();
		String signature = "HRFIEysBrgOiG7y3JmDJu0pzX9zwBuaWA9r1Z4InMChf4oVBEuIiujKawun+Qofo4lt9ZTxzYlLmnADyu9JFXnbs72wbUcYRXmaRVmU/0PznY9RpURW2f0Fwm0UWo9NR4n8QdG0vu8hYSL/1FcYg5Yqezfe+whdg1v50GCGWoDm0dfMzgh64BcYvW03B9LE4Rir5/2t4LOri8P5L7khuMh4Hka6bR3sFMt0mmsMEhyMzSjUr2HjetjUhujxubk0+k/JWHQXw86vBV9iiG+j05StWlVqbGn+pWyfnJ44Xmmhmqs9h5O4E7sdoLz+HmgmloFFFx9WIe7IOcbBAj+R7TA==";

		licenses.put( appId, CommonAppUtils.stringBase64ToByte( signature ) );

		appId = AppIds.R5.getAppId();
		signature = "M92IhlmJbDADdWABqjfuDU6BC1HRq68DIh7WrwKyVpCAooQX3m0ZEmVFfic8iyCOrayHTLA156ViR6XBvfae9IOW6/NTZ5YYMAu2+PWkaz3b0QSCzc0iNRIZjW6YbmpqTgWCc8B+d/Wh9XL2aeFDhHqOFGUxZfeHb+iDQ76Nx0J0wmUZNOeolgcGYnNI6vL29V7b5r7GJ4EUA4ATCOj9C4BiQ06iN1i2FFNvY+WM7F26TrDOr6CCZxEAkJDsZm3S1P1a3EKp8oT5JIguENkcnEKjco4NrN5Y/quRZTBxybEEB7CQYO45S3+obELkyycK0r7Uyd1itgw4Q+0bYhVxlA==";

		licenses.put( appId, CommonAppUtils.stringBase64ToByte( signature ) );

		appId = AppIds.IMAGE_RETENTION.getAppId();
		signature = "kmKa4Q/vflcUTLl016POAQC/Ilit710EhP4ajjnwt87EQnHPYZVH+qIrHDlMYYyaW5fKr0Lu3UXHLXb3ss0/2QfSNMpQhKV+fF+JPcwA2reqauzJ1g8YLq9XH08c8BQHBZBPp0Gu0ChCpCA4ZV6K8f8l+WjVONMKrxk+5hiku6z9PVSECWROqYjYJYmHbhYo4WIzBLGUzziyuCuTxe/LsQpLu2dVFHoMchtFHxoL02kPkZUeFWfE3arxNtiL2uI8oR850ocj52h0V634uRHSVnzb30N0HQ41yJ37XAmwSy4PwFMFJKO6AUnzbA/T9fLk6Wm+ddRAQU2GbrJMQROr0A==";

		licenses.put( appId, CommonAppUtils.stringBase64ToByte( signature ) );

		appId = AppIds.COMMAND_CLIENT_20.getAppId();
		signature = "hjfMRlFv9nnEpGCTOz9sRp6G5OXxThUqTLFnmlMT8fTmG9tlcnYfZvA4Pmfw5ZRPXQEODWskRB6gMbKTpP44yAk/fAYyKw6zjdCO1StcbSo/ddZdBhEejNiWTtwim9luyn9xE0JWq51w1PzHah1UUZlWjZbb2AZPePRyAao2vXSwmz2PMNTC2Yoa8plyaFNld0w8polh5FlFQ4ixH52fVtUgzoI1l9jb9AlSZw/m/S5oqyHnlQJ4b5NOkErl83xLAlFEjAJM41nr5eXfFiAQTkm7VCO+jVSXMUojERpuYRpSuQY1w0i7D8C6oj3lB7R5eiXkCESVuEwkD9wDbPTCUg==";

		licenses.put( appId, CommonAppUtils.stringBase64ToByte( signature ) );

		appId = AppIds.MOBILE_CLIENT.getAppId();
		signature = "bKhRGzttQ/5Jyt92/FDZtFAqFI3FUKjHNkkpXimARBfjQYPUD3F8Ilw9yRkmb7Ma/OaQieo30cSjWGy2syHxufl6GT8UbIT/8yYPQkb8d4tVvPLHZ7bCJm8uNR8+5e6yLDEU1isJ1TVlzT84092A+fHEB0UZ8jqlD7ZPV82ks6fUc8lRNdRxSwZKXyeWf3dSh63nAPjHeOZGJ4A+uUIIhJzh7SrBwnMzHJSLdhQeMKn7ylpDa5n9FjdOEo4y2PYk9L2gvpr0S/372eRPCErEV2oKfyTlXB96fXLiYD+dZgTnGs1oXpep8FGmvnVXdQp80lm/QtAfzitZW4VVuK+brw==";

		licenses.put( appId, CommonAppUtils.stringBase64ToByte( signature ) );
	}

	public static byte[] getSignature( String appId )
	{
		return ( byte[] ) licenses.get( appId );
	}

	public static boolean isPreLicensed( String appId )
	{
		return licenses.containsKey( appId );
	}
}
