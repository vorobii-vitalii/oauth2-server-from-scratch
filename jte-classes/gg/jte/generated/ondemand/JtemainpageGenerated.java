package gg.jte.generated.ondemand;
public final class JtemainpageGenerated {
	public static final String JTE_NAME = "main-page.jte";
	public static final int[] JTE_LINE_INFO = {2,2,2,2,2,2};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor) {
		jteOutput.writeContent("\n<h1>Main page!</h1>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
