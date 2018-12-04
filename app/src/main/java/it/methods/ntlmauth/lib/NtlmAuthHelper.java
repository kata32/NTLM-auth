package it.methods.ntlmauth.lib;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import jcifs.ntlmssp.NtlmFlags;
import jcifs.ntlmssp.Type1Message;
import jcifs.ntlmssp.Type2Message;
import jcifs.ntlmssp.Type3Message;
import jcifs.util.Base64;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class NtlmAuthHelper implements Authenticator
{
	private static final String CLASS_TAG = "NtlmAuthHelper";

	private static final String AUTHENTICATE_HEADERS = "WWW-Authenticate";
	private static final String HEADER_NEGOTIATE = "Negotiate";
	private static final String HEADER_NTLM = "NTLM";
	private static final String HEADER_AUTHORIZATION = "Authorization";

	private static final int TYPE_1_FLAGS =
		NtlmFlags.NTLMSSP_NEGOTIATE_56 |
		NtlmFlags.NTLMSSP_NEGOTIATE_128 |
		NtlmFlags.NTLMSSP_NEGOTIATE_NTLM2 |
		NtlmFlags.NTLMSSP_NEGOTIATE_ALWAYS_SIGN |
		NtlmFlags.NTLMSSP_REQUEST_TARGET;

	private String m_login;
	private String m_password;
	private String m_domain;
	private String m_workstation;

	private int m_contaAutenticazioni;
	private HttpUrl m_url;

	public NtlmAuthHelper(@NonNull String login, @NonNull String password)
	{
		this(login, password, Const.EMPTY_STRING, Const.EMPTY_STRING);
	}

	public NtlmAuthHelper(@NonNull String login, @NonNull String password, @NonNull String domain, @NonNull String workstation)
	{
		m_login = login;
		m_password = password;
		m_domain = domain;
		m_workstation = workstation;

		m_contaAutenticazioni = 0;
	}

	@Override
	public Request authenticate(Route route, Response response) throws IOException
	{
		if (m_url == null || response.request().url() != m_url)
		{
			m_contaAutenticazioni = 0;
			m_url = response.request().url();
		}

		m_contaAutenticazioni++;

		if (m_contaAutenticazioni >= 4)
			throw new IOException(String.format("challenge request count too big (%s)", m_contaAutenticazioni));

		List<String> authHeaders = response.headers(AUTHENTICATE_HEADERS);

		if (authHeaders != null)
		{
			boolean negotiate = false;
			boolean ntlm = false;
			String ntlmValue = null;

			for (String authHeader : authHeaders)
			{
				if (authHeader.equalsIgnoreCase(HEADER_NEGOTIATE))
				{
					negotiate = true;
				}
				if (authHeader.equalsIgnoreCase(HEADER_NTLM))
				{
					ntlm = true;
				}
				if (authHeader.startsWith(HEADER_NTLM + " "))
				{
					ntlmValue = authHeader.substring(5);
				}
			}

			if (negotiate && ntlm)
			{
				String type1Msg = generateType1Msg(m_domain, m_workstation);
				String header = HEADER_NTLM + " " + type1Msg;
				Log.v(CLASS_TAG, HEADER_AUTHORIZATION + " " + header);

				return response.request().newBuilder().header(HEADER_AUTHORIZATION, header).build();
			}
			else if (ntlmValue != null)
			{
				String type3Msg = generateType3Msg(m_login, m_password, m_domain, m_workstation, ntlmValue);
				String ntlmHeader = HEADER_NTLM + " " + type3Msg;
				Log.v(CLASS_TAG, HEADER_AUTHORIZATION + " " + ntlmHeader);

				return response.request().newBuilder().header(HEADER_AUTHORIZATION, ntlmHeader).build();
			}
		}

		if (responseCount(response) <= 3)
		{
			String credential = Credentials.basic(m_login, m_password);
			Log.v(CLASS_TAG, HEADER_AUTHORIZATION + " " + credential);

			return response.request().newBuilder().header(HEADER_AUTHORIZATION, credential).build();
		}

		return null;
	}

	private String generateType1Msg(@NonNull String domain, @NonNull String workstation)
	{
		final Type1Message type1Message = new Type1Message(TYPE_1_FLAGS, domain, workstation);
		byte[] source = type1Message.toByteArray();

		return Base64.encode(source);
	}

	private String generateType3Msg(final String login, final String password, final String domain, final String workstation, final String challenge)
	{
		Type2Message type2Message;

		try
		{
			byte[] decoded = Base64.decode(challenge);
			type2Message = new Type2Message(decoded);
		}
		catch (final IOException exception)
		{
			exception.printStackTrace();
			return null;
		}

		final int type2Flags = type2Message.getFlags();
		final int type3Flags = type2Flags & ~(NtlmFlags.NTLMSSP_TARGET_TYPE_DOMAIN | NtlmFlags.NTLMSSP_TARGET_TYPE_SERVER);
		final Type3Message type3Message = new Type3Message(type2Message, password, domain, login, workstation, type3Flags);

		return Base64.encode(type3Message.toByteArray());
	}

	private int responseCount(Response response)
	{
		int result = 1;
		while ((response = response.priorResponse()) != null)
		{
			if (!response.isRedirect())
				result++;
		}

		return result;
	}

}

