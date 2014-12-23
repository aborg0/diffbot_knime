/*
 *
 */
package com.diffbot.knime.analyze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;

/**
 * Settings for the Diffbot Article node.
 *
 * @author Gabor Bakos
 */
final class DiffbotAnalyzeNodeSettings {
	/**
	 *
	 */
	private static final String USE_ACCEPT_LANGUAGE = "useAcceptLanguage";
	/**
	 *
	 */
	private static final String USE_COOKIE = "useCookie";
	/**
	 *
	 */
	private static final String USE_REFERRER = "useReferrer";
	/**
	 *
	 */
	private static final String USE_USER_AGENT = "useUserAgent";
	/**
	 *
	 */
	private static final int DEFAULT_TIMEOUT = 30_000;
	/**
	 *
	 */
	static final int DEFAULT_VERSION = 3;
	/**
	 *
	 */
	static final String DEFAULT_OUTPUT_COLUMN = "Diffbot analysis";
	/**
	 *
	 */
	private static final String ACCEPT_LANGUAGE = "acceptLanguage";
	/**
	 *
	 */
	private static final String COOKIE = "cookie";
	/**
	 *
	 */
	private static final String REFERRER = "referrer";
	/**
	 *
	 */
	private static final String USER_AGENT = "userAgent";
	/**
	 *
	 */
	private static final String OUTPUT_COLUMN = "outputColumn";
	/**
	 * Configuration key for tokens.
	 */
	static final String TOKEN = "token";
	/**
	 *
	 */
	private static final String URL_COLUMN = "urlColumn";
	/**
	 * Configuration key for custom url.
	 */
	static final String URL = "URL";
	/**
	 *
	 */
	private static final String TIMEOUT_IN_MILLIS = "timeoutInMillis";
	/**
	 *
	 */
	private static final String VERSION = "version";
	/**
	 *
	 */
	private static final String FIELDS = "fields";
	/**
	 *
	 */
	private static final String MODE = "mode";
	private String token = null, url = "", urlColumn = "",
			outputColumn = DEFAULT_OUTPUT_COLUMN, fields = "", mode = "",
			userAgent = "", referrer = "", cookie = "", acceptLanguage = "";
	private int timeout = DEFAULT_TIMEOUT, version = DEFAULT_VERSION;
	private boolean useUserAgent = false, useReferrer = false,
			useCookie = false, useAcceptLanguage = false;

	/**
	 * Constructs the settings.
	 */
	public DiffbotAnalyzeNodeSettings() {
	}

	protected List<? extends String> validateConsistency()
			throws InvalidSettingsException {
		final List<String> warnings = new ArrayList<>();
		if (token == null || token.isEmpty()) {
			throw new InvalidSettingsException("No token was specified!");
		}
		if ((url == null || url.isEmpty())
				&& (urlColumn == null || urlColumn.isEmpty())) {
			throw new InvalidSettingsException(
					"No URL or column for URLs were specified!");
		}
		if (timeout < 0) {
			throw new InvalidSettingsException("Timeout should be positive!");
		}
		if (timeout < 1000 && timeout % 50 != 0) {
			warnings.add("Timeout is specified in milliseconds, not seconds!");
		}
		if (version < 2) {
			throw new InvalidSettingsException("Not supported version: "
					+ version);
		}
		if (version != 3) {
			warnings.add("Unknown version, API might have changed.");
		}
		if (outputColumn == null || outputColumn.isEmpty()) {
			throw new InvalidSettingsException(
					"Please specify output column name!");
		}
		if (Objects.equals(outputColumn, urlColumn)) {
			warnings.add("Cannot replace input column, new column name generated.");
		}
		return warnings;
	}

	/**
	 * Saves settings.
	 *
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		validateConsistency();
		settings.addString(URL, url);
		settings.addString(URL_COLUMN, urlColumn);
		settings.addString(OUTPUT_COLUMN, outputColumn);
		settings.addString(TOKEN, token);
		settings.addInt(VERSION, version);
		settings.addInt(TIMEOUT_IN_MILLIS, timeout);
		settings.addString(FIELDS, fields);
		settings.addString(MODE, mode);
		settings.addBoolean(USE_USER_AGENT, useUserAgent);
		settings.addString(USER_AGENT, userAgent);
		settings.addBoolean(USE_REFERRER, useReferrer);
		settings.addString(REFERRER, referrer);
		settings.addBoolean(USE_COOKIE, useCookie);
		settings.addString(COOKIE, cookie);
		settings.addBoolean(USE_ACCEPT_LANGUAGE, useAcceptLanguage);
		settings.addString(ACCEPT_LANGUAGE, acceptLanguage);
	}

	/**
	 * Loads settings for the dialog.
	 *
	 * @param settings
	 * @param specs
	 * @throws NotConfigurableException
	 */
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObjectSpec[] specs) throws NotConfigurableException {
		url = settings.getString(URL, "");
		urlColumn = settings.getString(URL_COLUMN, "");
		outputColumn = settings.getString(OUTPUT_COLUMN, "");
		token = settings.getString(TOKEN, null);
		outputColumn = settings.getString(OUTPUT_COLUMN, DEFAULT_OUTPUT_COLUMN);
		version = settings.getInt(VERSION, DEFAULT_VERSION);
		timeout = settings.getInt(TIMEOUT_IN_MILLIS, DEFAULT_TIMEOUT);
		fields = settings.getString(FIELDS, "");
		mode = settings.getString(MODE, "");
		useUserAgent = settings.getBoolean(USE_USER_AGENT, false);
		userAgent = settings.getString(USER_AGENT, null);
		useReferrer = settings.getBoolean(USE_REFERRER, false);
		referrer = settings.getString(REFERRER, null);
		useCookie = settings.getBoolean(USE_COOKIE, false);
		cookie = settings.getString(COOKIE, null);
		useAcceptLanguage = settings.getBoolean(USE_ACCEPT_LANGUAGE, false);
		acceptLanguage = settings.getString(ACCEPT_LANGUAGE, null);
	}

	/**
	 * Loads settings for the node model.
	 *
	 * @param settings
	 * @throws InvalidSettingsException
	 */
	public void loadSettingsFrom(final NodeSettingsRO settings)
			throws InvalidSettingsException {
		url = settings.getString(URL);
		urlColumn = settings.getString(URL_COLUMN);
		outputColumn = settings.getString(OUTPUT_COLUMN);
		token = settings.getString(TOKEN);
		outputColumn = settings.getString(OUTPUT_COLUMN);
		version = settings.getInt(VERSION);
		timeout = settings.getInt(TIMEOUT_IN_MILLIS);
		fields = settings.getString(FIELDS);
		mode = settings.getString(MODE);
		useUserAgent = settings.getBoolean(USE_USER_AGENT);
		userAgent = settings.getString(USER_AGENT);
		useReferrer = settings.getBoolean(USE_REFERRER);
		referrer = settings.getString(REFERRER);
		useCookie = settings.getBoolean(USE_COOKIE);
		cookie = settings.getString(COOKIE);
		useAcceptLanguage = settings.getBoolean(USE_ACCEPT_LANGUAGE);
		acceptLanguage = settings.getString(ACCEPT_LANGUAGE);
	}

	Map<String, String> settingsAsMap() {
		final Map<String, String> ret = new LinkedHashMap<>();
		if (fields != null && !fields.isEmpty()) {
			ret.put("fields", fields);
		}
		if (mode != null && !mode.isEmpty()) {
			ret.put("mode", mode);
		}
		ret.put("timeout", String.valueOf(timeout));
		addHeader(ret, "X-Forward-User-Agent", userAgent);
		addHeader(ret, "X-Forward-Referer", referrer);
		addHeader(ret, "X-Forward-Cookie", cookie);
		addHeader(ret, "X-Forward-Accept-Language", acceptLanguage);
		return Collections.unmodifiableMap(ret);
	}

	/**
	 * @param ret
	 * @param key
	 * @param value
	 */
	private void addHeader(final Map<String, String> ret, final String key,
			final String value) {
		if (value != null && !value.isEmpty()) {
			ret.put(key, value);
		}
	}

	/**
	 * @return the token
	 */
	final String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	final void setToken(final String token) {
		this.token = token;
	}

	/**
	 * @return the url
	 */
	final String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	final void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * @return the urlColumn
	 */
	final String getUrlColumn() {
		return urlColumn;
	}

	/**
	 * @param urlColumn
	 *            the urlColumn to set
	 */
	final void setUrlColumn(final String urlColumn) {
		this.urlColumn = urlColumn;
	}

	/**
	 * @return the outputColumn
	 */
	final String getOutputColumn() {
		return outputColumn;
	}

	/**
	 * @param outputColumn
	 *            the outputColumn to set
	 */
	final void setOutputColumn(final String outputColumn) {
		this.outputColumn = outputColumn;
	}

	/**
	 * @return the userAgent
	 */
	final String getUserAgent() {
		return userAgent;
	}

	/**
	 * @param userAgent
	 *            the userAgent to set
	 */
	final void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * @return the referrer
	 */
	final String getReferrer() {
		return referrer;
	}

	/**
	 * @param referrer
	 *            the referrer to set
	 */
	final void setReferrer(final String referrer) {
		this.referrer = referrer;
	}

	/**
	 * @return the cookie
	 */
	final String getCookie() {
		return cookie;
	}

	/**
	 * @param cookie
	 *            the cookie to set
	 */
	final void setCookie(final String cookie) {
		this.cookie = cookie;
	}

	/**
	 * @return the acceptLanguage
	 */
	final String getAcceptLanguage() {
		return acceptLanguage;
	}

	/**
	 * @param acceptLanguage
	 *            the acceptLanguage to set
	 */
	final void setAcceptLanguage(final String acceptLanguage) {
		this.acceptLanguage = acceptLanguage;
	}

	/**
	 * @return the timeout
	 */
	final int getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	final void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	/**
	 * @return the version
	 */
	final int getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	final void setVersion(final int version) {
		this.version = version;
	}

	/**
	 * @return the fields
	 */
	final String getFields() {
		return fields;
	}

	/**
	 * @param fields
	 *            the fields to set
	 */
	final void setFields(final String fields) {
		this.fields = fields;
	}

	/**
	 * @return the mode
	 */
	final String getMode() {
		return mode;
	}

	/**
	 * @param mode
	 *            the mode to set
	 */
	final void setMode(final String mode) {
		this.mode = mode;
	}

	/**
	 * @return the useUserAgent
	 */
	final boolean isUseUserAgent() {
		return useUserAgent;
	}

	/**
	 * @param useUserAgent
	 *            the useUserAgent to set
	 */
	final void setUseUserAgent(final boolean useUserAgent) {
		this.useUserAgent = useUserAgent;
	}

	/**
	 * @return the useReferrer
	 */
	final boolean isUseReferrer() {
		return useReferrer;
	}

	/**
	 * @param useReferrer
	 *            the useReferrer to set
	 */
	final void setUseReferrer(final boolean useReferrer) {
		this.useReferrer = useReferrer;
	}

	/**
	 * @return the useCookie
	 */
	final boolean isUseCookie() {
		return useCookie;
	}

	/**
	 * @param useCookie
	 *            the useCookie to set
	 */
	final void setUseCookie(final boolean useCookie) {
		this.useCookie = useCookie;
	}

	/**
	 * @return the useAcceptLanguage
	 */
	final boolean isUseAcceptLanguage() {
		return useAcceptLanguage;
	}

	/**
	 * @param useAcceptLanguage
	 *            the useAcceptLanguage to set
	 */
	final void setUseAcceptLanguage(final boolean useAcceptLanguage) {
		this.useAcceptLanguage = useAcceptLanguage;
	}
}
