package io.gravitee.gateway.platforms.servlet;

import io.gravitee.gateway.http.ContentRequest;
import io.gravitee.gateway.http.Request;
import org.eclipse.jetty.http.HttpHeader;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 *
 * @author David BRASSEY (brasseld at gmail.com)
 */
public class RequestBuilder extends Request {

	public static Request from(HttpServletRequest servletRequest) throws IOException {

		Request request = null;

		if (hasContent(servletRequest)) {
			request = new ContentRequest(servletRequest.getInputStream());
		} else {
			request = new Request();
		}

		copyHeaders(request, servletRequest);
		copyQueryParameters(request, servletRequest);

		return request;
	}

	private static boolean hasContent(HttpServletRequest servletRequest)
	{
		return servletRequest.getContentLength() > 0 ||
				servletRequest.getContentType() != null ||
				servletRequest.getHeader(HttpHeader.TRANSFER_ENCODING.asString()) != null;
	}

	private static void copyHeaders(Request request, HttpServletRequest servletRequest) {
		Enumeration<String> headerNames = servletRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String hname = headerNames.nextElement();
			String hval = servletRequest.getHeader(hname);
			request.getHeaders().put(hname, hval);
		}
	}

	private static void copyQueryParameters(Request request, HttpServletRequest servletRequest) {
		String query = servletRequest.getQueryString();

		if (query != null) {
			try {
				query = URLDecoder.decode(query, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}

			String[] pairSplit = query.split("&");
			for (String paramPair : pairSplit) {
				int idx = paramPair.indexOf("=");
				if (idx != -1) {
					String key = paramPair.substring(0, idx);
					String val = paramPair.substring(idx + 1);
					request.getQueryParameters().put(key, val);
				} else {
					request.getQueryParameters().put(paramPair, null);
				}
			}
		}
	}
}