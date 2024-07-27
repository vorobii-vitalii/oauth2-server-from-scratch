package gg.jte.generated.ondemand;
public final class JteprovideconsentpageGenerated {
	public static final String JTE_NAME = "provide-consent-page.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,4,4,4,4,5,5,5,10};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, String clientName, String clientDescription) {
		jteOutput.writeContent("\n<h1>Consent page</h1>\n<h3>");
		jteOutput.setContext("h3", null);
		jteOutput.writeUserContent(clientName);
		jteOutput.writeContent(" is asking for permissions</h3>\n<p>");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(clientDescription);
		jteOutput.writeContent("</p>\n\n<div>\n    <button onclick=\"\">Approve</button>\n    <button onclick=\"\">Reject</button>\n</div>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		String clientName = (String)params.get("clientName");
		String clientDescription = (String)params.get("clientDescription");
		render(jteOutput, jteHtmlInterceptor, clientName, clientDescription);
	}
}
