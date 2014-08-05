package playbasis.pblib;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.util.JsonReader;

/**
 * The Playbasis Object
 * @author Playbasis Team
 */
public class Playbasis
{
	public static Playbasis instance;
	
	private static final String BASE_URL = "https://api.pbapp.net/";
	private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
	private static final String CHARSET = "UTF-8";
	
	private String token;
	private String apiKeyParam;
	
	public Playbasis()
	{
		assert instance == null;
		instance = this;
	}
	
	/**
	 * Authentication procedure on playbasis
	 * @param apiKey
	 * @param apiSecret
	 * @return true if request is successful
	 */
	public boolean auth(String apiKey, String apiSecret)
	{
		apiKeyParam = "?api_key=" + apiKey;
		String param = "";
		try
		{
			param = "api_key=" + URLEncoder.encode(apiKey, "UTF-8") + 
					"&api_secret=" + URLEncoder.encode(apiSecret, "UTF-8");	
		}
		catch (UnsupportedEncodingException e)
		{
			return false;
		}
		try
		{
			JsonReader reader = callJSON("Auth", param);
			while(true)
			{
				switch(reader.peek())
				{
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case BEGIN_ARRAY:
					reader.beginArray();
					break;
				case END_ARRAY:
					reader.endArray();
					break;
				case END_DOCUMENT:
					reader.close();
					return false;
				case NAME:
					String name = reader.nextName();
					if(name.equals("token"))
					{
						token = reader.nextString();
						reader.close();
						return true;
					}
					break;
				default:
					reader.skipValue();
					break;
				}
				
			}
		}
		catch (IOException e)
		{
			return false;
		}
	}
	
	/**
	 * Renew the authentification
	 * @param apiKey
	 * @param apiSecret
	 * @return true if request is successful
	 */
	public boolean renew(String apiKey, String apiSecret)
	{
		apiKeyParam = "?api_key=" + apiKey;
		String param = "";
		try
		{
			param = "api_key=" + URLEncoder.encode(apiKey, "UTF-8") + 
					"&api_secret=" + URLEncoder.encode(apiSecret, "UTF-8");	
		}
		catch (UnsupportedEncodingException e)
		{
			return false;
		}
		try
		{
			JsonReader reader = callJSON("Auth/renew", param);
			while(true)
			{
				switch(reader.peek())
				{
				case BEGIN_OBJECT:
					reader.beginObject();
					break;
				case END_OBJECT:
					reader.endObject();
					break;
				case BEGIN_ARRAY:
					reader.beginArray();
					break;
				case END_ARRAY:
					reader.endArray();
					break;
				case END_DOCUMENT:
					reader.close();
					return false;
				case NAME:
					String name = reader.nextName();
					if(name.equals("token"))
					{
						token = reader.nextString();
						reader.close();
						return true;
					}
					break;
				default:
					reader.skipValue();
					break;
				}
				
			}
		}
		catch (IOException e)
		{
			return false;
		}
	}
	
	/**
	 * Get information for a player. Fields include
	 * image
	 * email
	 * username
	 * exp
	 * level
	 * first_name
	 * last_name
	 * gender
	 * birth_date
	 * registered
	 * last_login
	 * last_logout
	 * cl_player_id
	 * @param playerId
	 * @return
	 */
	public JsonReader player(String playerId)
	{
		return callJSON("Player/"+playerId, "token="+token);
	}
	/*
	 * Get detailed information about a player, including points and badges
	 */
	public JsonReader playerDetail(String playerId)
	{
		return callJSON("Player/"+playerId+"/data/all", "token="+token);
	}
	/*
	 * playerListId player id as used in client's website separate with ',' example '1,2,3'
	 */
	public JsonReader playerList(String playerListId)
	{
		StringBuilder param = new StringBuilder();
		try
		{
			param.append("token=");
			param.append(token);
			param.append("&list_player_id=");
			param.append(URLEncoder.encode(playerListId, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		return callJSON("Player/list", param.toString());
	}
	
	/*
	 * @param	optionalData	Varargs of String for additional parameters to be sent to the register method.
	 * 							Each element is a string in the format of key=value, for example: first_name=john
	 * 							The following keys are supported:
	 * 							- facebook_id
	 * 							- twitter_id
	 * 							- password		assumed hashed
	 * 							- first_name
	 * 							- last_name
	 * 							- nickname
	 * 							- gender		1=Male, 2=Female
	 * 							- birth_date	format YYYY-MM-DD
	 */
	public JsonReader register(String playerId, String username, String email, String imageUrl, String... optionalData)
	{
		StringBuilder param = new StringBuilder();
		try
		{
			param.append("token=");
			param.append(token);
			param.append("&username=");
			param.append(URLEncoder.encode(username, "UTF-8"));
			param.append("&email=");
			param.append(URLEncoder.encode(email, "UTF-8"));
			param.append("&image=");
			param.append(URLEncoder.encode(imageUrl, "UTF-8"));
			
			for(int i=0; i<optionalData.length; ++i)
				param.append("&"+optionalData[i]);
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		return callJSON("Player/"+playerId+"/register", param.toString());
	}
	
	/*
	 * @param	updateData	    Key-value for data to be updated.
	 * 							The following keys are supported:
	  *							- username
	 *							- email
	 *							- image
	 *							- exp
	 *							- level
	 * 							- facebook_id
	 * 							- twitter_id
	 * 							- password		assumed hashed
	 * 							- first_name
	 * 							- last_name
	 * 							- nickname
	 * 							- gender		1=Male, 2=Female
	 * 							- birth_date	format YYYY-MM-DD
	 */
	public JsonReader update(String playerId, String... updateData) throws UnsupportedEncodingException
	{
		StringBuilder param = new StringBuilder();
		param.append("token=");
		param.append(token);
		
		for(int i=0; i<updateData.length; ++i)
			param.append("&"+updateData[i]);
		
		return callJSON("Player/"+playerId+"/update", param.toString());
	}
	
	/**
     * Delete a player
     * @param playerId
     * @return
     */
	public JsonReader delete(String playerId) throws UnsupportedEncodingException
	{		
		return callJSON("Player/"+playerId+"/delete", "token="+token);
	}
	
	/**
	 * Call login action on server
	 * @param playerId
	 * @return
	 */
	public JsonReader login(String playerId)
	{
		return callJSON("Player/"+playerId+"/login", "token="+token);
	}
	
	/**
	 * Call logout action on server
	 * @param playerId
	 * @return
	 */
	public JsonReader logout(String playerId)
	{
		return callJSON("Player/"+playerId+"/logout", "token="+token);
	}
	
	/**
	 * Returns information about all point-based rewards that a player currently have.
	 * @param playerId
	 * @return
	 */
	public JsonReader points(String playerId)
	{
		return callJSON("Player/"+playerId+"/points"+apiKeyParam, null);
	}
	
	/**
	 * Returns how much of specified the point-based reward a player currently have.
	 * @param playerId
	 * @param pointName
	 * @return
	 */
	public JsonReader point(String playerId, String pointName)
	{
		return callJSON("Player/"+playerId+"/point/"+pointName+apiKeyParam, null);
	}
	
	/**
     * Returns reward a player currently have.
     * @param playerId
     * @param pointName
     * @param offset
     * @param limit
     * @return
     */
	public JsonReader pointHistory(String playerId, String pointName, int offset, int limit)
    {
        String stringQuery = "&offset="+offset+"&limit"+limit;
        if(!pointName.isEmpty() && pointName != null){
            stringQuery = stringQuery+"&point_name="+pointName;
        }
        return callJSON("Player/"+playerId+"/point/"+pointName+apiKeyParam+stringQuery, null);
    }
	
	/**
	 * Returns the time and action that a player last performed.
	 * @param playerId
	 * @return
	 */
	public JsonReader actionLastPerformed(String playerId)
	{
		return callJSON("Player/"+playerId+"/action/time"+apiKeyParam, null);
	}
	
	/**
	 * Returns the last time that player performed the specified action.
	 * @param playerId
	 * @param actionName
	 * @return
	 */
	public JsonReader actionLastPerformedTime(String playerId, String actionName)
	{
		return callJSON("Player/"+playerId+"/action/"+actionName+"/time"+apiKeyParam, null);
	}
	
	/**
	 * Returns the number of times that a player has performed the specified action.
	 * @param playerId
	 * @param actionName
	 * @return
	 */
	public JsonReader actionPerformedCount(String playerId, String actionName)
	{
		return callJSON("Player/"+playerId+"/action/"+actionName+"/count"+apiKeyParam, null);
	}
	
	/**
	 * Returns information about all the badges that a player has earned.
	 * @param playerId
	 * @return
	 */
	public JsonReader badgeOwned(String playerId)
	{
		return callJSON("Player/"+playerId+"/badge"+apiKeyParam, null);
	}
	
	/**
	 * Returns list of top players according to specified point type.
	 * @param rankedBy
	 * @param limit
	 * @return
	 */
	public JsonReader rank(String rankedBy, int limit)
	{
		return callJSON("Player/rank/"+rankedBy+"/"+String.valueOf(limit)+apiKeyParam, null);
	}
	
	/**
	 * Returns list of top players.
	 * @param limit
	 * @return
	 */
	public JsonReader ranks(int limit)
	{
		return callJSON("Player/ranks/"+String.valueOf(limit)+apiKeyParam, null);
	}
	
	/**
	 * Returns information about specified level.
	 * @param lv
	 * @return
	 */
	public JsonReader level(int lv)
	{
		return callJSON("Player/level/"+String.valueOf(lv)+apiKeyParam, null);
	}
     /**
	 * Returns information of all levels.
	 * @return
	 */
	public JsonReader levels()
	{
		return callJSON("Player/levels/"+apiKeyParam, null);
	}
	
	public JsonReader claimBadge(String playerId, String badgeId){
		StringBuilder param = new StringBuilder();
		param.append("token=");
		param.append(token);
		
		return callJSON("Player/"+playerId+"/badge/"+badgeId+"/claim"+apiKeyParam, param.toString());	
	}
	public JsonReader redeemBadge(String playerId, String badgeId){
		StringBuilder param = new StringBuilder();
		param.append("token=");
		param.append(token);
		
		return callJSON("Player/"+playerId+"/badge/"+badgeId+"/redeem"+apiKeyParam, param.toString());	
	}
	
	/**
	 * Returns information about all the goods list that a player has redeem.
	 * @param playerId player id as used in client's website
	 * @return
	 */
	public JsonReader playerGoods(String playerId)
	{
		return callJSON("Player/"+playerId+"/goods"+apiKeyParam, null);
	}
	
	public JsonReader questOfPlayer(String playerId, String questId)
	{
		return callJSON("Player/quest/"+questId+apiKeyParam+"&player_id="+playerId, null);
	}
	
	public JsonReader questListOfPlayer(String playerId)
	{
		return callJSON("Player/quest"+apiKeyParam+"&player_id="+playerId, null);
	}
	
	/**
	 * Returns information about all available badges for the current site.
	 * @return
	 */
	public JsonReader badges()
	{
		return callJSON("Badge"+apiKeyParam, null);
	}
	
	public JsonReader badge(String badgeId)
	{
		return callJSON("Badge/"+badgeId+apiKeyParam, null);
	}
	
	/**
	 * Returns information about the goods with the specified id.
	 * @param goodId
	 * @return
	 */
	public JsonReader goodInfo(String goodId)
	{
		return callJSON("Goods/"+goodId+apiKeyParam, null);
	}
	/**
	 * Returns information about all available goods for the current site.
	 * @return
	 */
	public JsonReader goodsList()
	{
		return callJSON("Goods/"+apiKeyParam, null);
	}
	
	/**
	 * Returns names of actions that can trigger game rules within a client’s website.
	 * @return
	 */
	public JsonReader actionConfig()
	{
		return callJSON("Engine/actionConfig"+apiKeyParam, null);
	}
	
	/*
	 * @param	optionalData	Varargs of String for additional parameters to be sent to the rule method.
	 * 							Each element is a string in the format of key=value, for example: url=playbasis.com
	 * 							The following keys are supported:
	 * 							- url		url or filter string (for triggering non-global actions)
	 * 							- reward	name of the custom-point reward to give (for triggering rules with custom-point reward)
	 * 							- quantity	amount of points to give (for triggering rules with custom-point reward)
	 */
	public JsonReader rule(String playerId, String action, String... optionalData)
	{
		StringBuilder param = new StringBuilder();
		try
		{
			param.append("token=");
			param.append(token);
			param.append("&player_id=");
			param.append(URLEncoder.encode(playerId, "UTF-8"));
			param.append("&action=");
			param.append(URLEncoder.encode(action, "UTF-8"));
			
			for(int i=0; i<optionalData.length; ++i)
				param.append("&"+optionalData[i]);
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		return callJSON("Engine/rule", param.toString());	
	}
	
	/**
	 * Returns information about the quest with the specified id.
	 * @param questId
	 * @return
	 */
	public JsonReader quest(String questId)
	{
		return callJSON("Quest/"+questId+apiKeyParam, null);
	}
	/**
	 * Returns information about all quest for the current site.
	 * @return
	 */
	public JsonReader quests()
	{
		return callJSON("Quest"+apiKeyParam, null);
	}
	
	/**
	 * Returns information about mission with the specified id.
	 * @param questId
	 * @param missionId
	 * @return
	 */
	public JsonReader mission(String questId, String missionId)
	{
		return callJSON("Quest/"+questId+"/mission/"+missionId+apiKeyParam, null);
	}
	
	/**
	 * Returns information about all available quest for the player.
	 * @param playerId
	 * @return
	 */
	public JsonReader questsAvailable(String playerId)
	{
		return callJSON("Quest/available"+apiKeyParam+"&player_id="+playerId, null);
	}
	
	/**
	 * check the quest is available/unavailable for player.
	 * @param questId
	 * @param playerId
	 * @return
	 */
	public JsonReader questAvailable(String questId, String playerId)
	{
		return callJSON("Quest/"+questId+"/available"+apiKeyParam+"&player_id="+playerId, null);
	}
	
	public JsonReader joinQuest(String questId, String playerId)
	{	
		StringBuilder param = new StringBuilder();
		try
		{
			param.append("token=");
			param.append(token);
			param.append("&player_id=");
			param.append(URLEncoder.encode(playerId, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		
		return callJSON("Quest/"+questId+"/join", param.toString());
	}
	
	public JsonReader cancelQuest(String questId, String playerId)
	{	
		StringBuilder param = new StringBuilder();
		try
		{
			param.append("token=");
			param.append(token);
			param.append("&player_id=");
			param.append(URLEncoder.encode(playerId, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		
		return callJSON("Quest/"+questId+"/cancel", param.toString());
	}
	
	public JsonReader redeemGoods(String goodsId, String playerId, int amount)
	{	
		StringBuilder param = new StringBuilder();
		try
		{
			param.append("token=");
			param.append(token);
			param.append("&quest_id=");
			param.append(URLEncoder.encode(playerId, "UTF-8"));
			param.append("&player_id=");
			param.append(URLEncoder.encode(playerId, "UTF-8"));
			param.append("&amount=");
			param.append(URLEncoder.encode(playerId, "UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
		
		return callJSON("Redeem/goods", param.toString());
	}
	
	public JsonReader recentPoint(int offset, int limit)
	{
		String stringQuery = "&offset="+offset+"&limit"+limit;
		return callJSON("Service/recent_point"+apiKeyParam+stringQuery, null);
	}

	public JsonReader recentPointByName(String pointName, int offset, int limit)
	{
		String stringQuery = "&offset="+offset+"&limit"+limit;
        if(!pointName.isEmpty() && pointName != null){
            stringQuery = stringQuery+"&point_name="+pointName;
        }
		return callJSON("Service/recent_point"+apiKeyParam+stringQuery, null);
	}
	
	public static String call(String method, String data)
	{
		try
		{
			return MakeRequest(new URL(BASE_URL + method), data);
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	public static JsonReader callJSON(String method, String data)
	{
		return new JsonReader(new StringReader(call(method, data)));
	}
	
	private static String MakeRequest(URL url, String data)
	{
		try
		{
			//opening http or https connection
			HttpURLConnection http = null;
			if (url.getProtocol().contains("https") || url.getProtocol().contains("HTTPS"))
			{
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				http = https;
			}
			else //regular http connection
			{
				http = (HttpURLConnection) url.openConnection();
			}

			//set method to post if we have data to send
			if (data != null)
			{
				http.setRequestMethod("POST");
				http.setDoOutput(true);
				http.setUseCaches(false);
				http.setRequestProperty("Content-Type", CONTENT_TYPE);
				http.setRequestProperty("charset", CHARSET);
				http.setFixedLengthStreamingMode(data.getBytes().length); //http.setChunkedStreamingMode(0);
				
				//write data
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(http.getOutputStream()));
				out.write(data);
				out.flush();
			}
			else
			{
				http.setRequestMethod("GET");
			}
			
			//get the response string
			InputStream in = http.getInputStream();
			assert in != null;
			BufferedReader rd = new BufferedReader(new InputStreamReader(in));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null)
				sb.append(line);
			rd.close();
			
			http.disconnect();
			return sb.toString();
		}
		catch (IOException e)
		{
			return null;
		}
	}

	////////////////////////////////////////
	// Below:
	// code accept all host, don't check for any certificate.
	// from:
	// http://stackoverflow.com/questions/995514/https-connection-android/1000205#1000205
	////////////////////////////////////////

	/**
	 * always verify the host - don't check for certificate
	 */
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier()
	{
		public boolean verify(String hostname, SSLSession session)
		{
			return true;
		}
	};

	/**
	 * Trust every server - don't check for any certificate
	 */
	private static void trustAllHosts()
	{
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]
		{ 
			new X509TrustManager()
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return new java.security.cert.X509Certificate[] {};
				}
	
				public void checkClientTrusted(X509Certificate[] chain,	String authType) throws CertificateException
				{
				}
	
				public void checkServerTrusted(X509Certificate[] chain,	String authType) throws CertificateException
				{
				}
			}
		};

		try // Install the all-trusting trust manager
		{
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}