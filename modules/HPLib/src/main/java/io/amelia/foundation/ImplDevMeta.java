/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

public interface ImplDevMeta
{
	String KEY_PRODUCT_NAME = "productName";
	String KEY_PRODUCT_DESCRIPTION = "productDescription";
	String KEY_PRODUCT_COPYRIGHT = "productCopyright";
	String KEY_VERSION_MAJOR = "versionMajor";
	String KEY_VERSION_MINOR = "versionMinor";
	String KEY_VERSION_REVISION = "versionRevision";
	String KEY_BUILD_NUMBER = "versionBuild";
	String KEY_CODENAME = "codename";
	String KEY_GIT_REPO = "gitRepo";
	String KEY_GIT_REPO_URL = "gitRepoUrl";
	String KEY_GIT_BRANCH = "gitBranch";
	String KEY_DEV_NAME = "devName";
	String KEY_DEV_EMAIL = "devEmail";
	String KEY_DEV_LICENSE = "devLicense";

	/**
	 * Get the server build number
	 * The build number is only set when the server is built on our Jenkins Build Server or by Travis,
	 * meaning this will be 0 for all development builds
	 *
	 * @return The server build number
	 */
	default String getBuildNumber()
	{
		return getProperty( KEY_BUILD_NUMBER );
	}

	default String getCodeName()
	{
		return getProperty( KEY_CODENAME );
	}

	/**
	 * Get the developer e-mail address
	 * Suggested use is to report problems
	 *
	 * @return The developer e-mail address
	 */
	default String getDeveloperEmail()
	{
		return getProperty( KEY_DEV_EMAIL );
	}

	default String getDeveloperName()
	{
		return getProperty( KEY_DEV_NAME );
	}

	/**
	 * Get the GitHub Branch this was built from, e.g., master
	 * Set by the Gradle build script
	 *
	 * @return The GitHub branch
	 */
	default String getGitBranch()
	{
		return getProperty( KEY_GIT_BRANCH );
	}

	default String getGitRepo()
	{
		return getProperty( KEY_GIT_REPO_URL );
	}

	default String getGitRepoUrl()
	{
		return getProperty( KEY_GIT_REPO );
	}

	/**
	 * Generates a HTML suitable footer for general server info and exception pages
	 *
	 * @return HTML footer string
	 */
	default String getHTMLFooter()
	{
		return "<small>Running <a href=\"" + getGitRepoUrl() + "\">" + getProductName() + "</a> Version " + getVersionDescribe() + " (Build #" + getBuildNumber() + ")<br />" + getProductCopyright() + "</small>";
	}

	/**
	 * Get the server copyright, e.g., Copyright (c) 2015 Chiori-chan
	 *
	 * @return The server copyright
	 */
	default String getLicense()
	{
		return getProperty( KEY_DEV_LICENSE );
	}

	/**
	 * Get the server copyright, e.g., Copyright (c) 2015 Chiori-chan
	 *
	 * @return The server copyright
	 */
	default String getProductCopyright()
	{
		return getProperty( KEY_PRODUCT_COPYRIGHT );
	}

	/**
	 * Describe this product
	 *
	 * @return The Product Name with Version
	 */
	default String getProductDescribed()
	{
		return "Running " + getProductName() + " version " + getVersionDescribe();
	}

	default String getProductDescription()
	{
		return getProperty( KEY_PRODUCT_DESCRIPTION );
	}

	/**
	 * Get the server product name, e.g., Honey Pot Server
	 *
	 * @return The Product Name
	 */
	default String getProductName()
	{
		return getProperty( KEY_PRODUCT_NAME );
	}

	String getProperty( String key );

	/**
	 * Get the server version number, e.g., 9.2.1
	 *
	 * @return The server version number
	 */
	default String getVersion()
	{
		return getProperty( KEY_VERSION_MAJOR ) + "." + getProperty( KEY_VERSION_MINOR ) + "." + getProperty( KEY_VERSION_REVISION );
	}

	/**
	 * Get the version, e.g., 9.2.1 Build #108 (Milky Berry)
	 *
	 * @return The version with code name
	 */
	default String getVersionDescribe()
	{
		String build = getBuildNumber();
		build = Integer.parseInt( build ) > 0 ? " Build #" + build : "";
		return getVersion() + build + " (" + getCodeName() + ")";
	}
}
