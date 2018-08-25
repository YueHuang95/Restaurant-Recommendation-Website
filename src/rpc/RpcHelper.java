package rpc;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.json.*;

public class RpcHelper {
	private static final String TYPE = "application/json";
	private static final String ALLOWED = "Access-Control-Allow-Origin";
	// write a JSONArray to http response
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType(TYPE);
		response.addHeader(ALLOWED, "*");
		out.print(array);
		out.close();
	}
	//write a JSONObject to http response
	public static void writeJsonObject (HttpServletResponse response, JSONObject obj) throws IOException {
		PrintWriter out = response.getWriter();
		response.setContentType(TYPE);
		response.addHeader(ALLOWED, "*");
		out.print(obj);
		out.close();
	}
}
