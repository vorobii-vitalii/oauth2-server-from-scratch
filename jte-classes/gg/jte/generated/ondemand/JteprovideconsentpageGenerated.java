package gg.jte.generated.ondemand;
public final class JteprovideconsentpageGenerated {
	public static final String JTE_NAME = "provide-consent-page.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,7,7,7,7,9,9,9,23,28,28,28,30,30,30,44,49,49,49,50,50,50,51,51,53,53,56,56,57,57,57,58,58,60,60,65};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String clientName, String clientDescription, java.util.Collection<String> scopeList, String authorizationRequestId) {
		jteOutput.writeContent("\n<script>\n  async function approveRequest() {\n    console.log(\"Going to approve ");
		jteOutput.setContext("script", null);
		jteOutput.writeUserContent(authorizationRequestId);
		jteOutput.writeContent("\");\n    try {\n      const loginResponse = await fetch('/approve/");
		jteOutput.setContext("script", null);
		jteOutput.writeUserContent(authorizationRequestId);
		jteOutput.writeContent("', {\n        method: 'POST',\n        headers: {\n          'Content-type': 'application/json; charset=UTF-8'\n        }\n      });\n      const jsonResponse = await loginResponse.json();\n      console.log(\"JSON response\");\n      console.log(JSON.stringify(jsonResponse));\n      window.location.href = jsonResponse.redirectURL;\n    }\n    catch (err) {\n      console.error(\"Error on approve...\");\n      console.error(err);\n      ");
		jteOutput.writeContent("\n    }\n  }\n\n  async function rejectRequest() {\n    console.log(\"Going to reject ");
		jteOutput.setContext("script", null);
		jteOutput.writeUserContent(authorizationRequestId);
		jteOutput.writeContent("\");\n    try {\n      const loginResponse = await fetch('/reject/");
		jteOutput.setContext("script", null);
		jteOutput.writeUserContent(authorizationRequestId);
		jteOutput.writeContent("', {\n        method: 'POST',\n        headers: {\n          'Content-type': 'application/json; charset=UTF-8'\n        }\n      });\n      const jsonResponse = await loginResponse.json();\n      console.log(\"JSON response\");\n      console.log(JSON.stringify(jsonResponse));\n      window.location.href = jsonResponse.redirectURL;\n    }\n    catch (err) {\n      console.error(\"Error on reject...\");\n      console.error(err);\n      ");
		jteOutput.writeContent("\n    }\n  }\n</script>\n\n<h1>");
		jteOutput.setContext("h1", null);
		jteOutput.writeUserContent(clientName);
		jteOutput.writeContent("</h1>\n<p>");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(clientDescription);
		jteOutput.writeContent("</p>\n");
		if (scopeList.isEmpty()) {
			jteOutput.writeContent("\n    <h3>This app is asking for all permissions on your account</h3>\n");
		} else {
			jteOutput.writeContent("\n    <h3>This app is asking for following permissions on your account</h3>\n    <ul>\n        ");
			for (var permission : scopeList) {
				jteOutput.writeContent("\n            <li>");
				jteOutput.setContext("li", null);
				jteOutput.writeUserContent(permission);
				jteOutput.writeContent("</li>\n        ");
			}
			jteOutput.writeContent("\n    </ul>\n");
		}
		jteOutput.writeContent("\n\n<div>\n    <button onclick=\"approveRequest()\">Approve</button>\n    <button onclick=\"rejectRequest()\">Reject</button>\n</div>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String clientName = (String)params.get("clientName");
		String clientDescription = (String)params.get("clientDescription");
		java.util.Collection<String> scopeList = (java.util.Collection<String>)params.get("scopeList");
		String authorizationRequestId = (String)params.get("authorizationRequestId");
		render(jteOutput, jteHtmlInterceptor, clientName, clientDescription, scopeList, authorizationRequestId);
	}
}
