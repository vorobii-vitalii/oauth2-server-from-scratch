package gg.jte.generated.ondemand;
import java.util.stream.Collectors;
import api.security.training.dto.LoginPageParams;
public final class JteloginGenerated {
	public static final String JTE_NAME = "login.jte";
	public static final int[] JTE_LINE_INFO = {0,0,1,3,3,3,27,27,27,27,56};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, LoginPageParams loginPageParams) {
		jteOutput.writeContent("\n<h1>Login</h1>\n<h3>Enter your login credentials</h3>\n<script>\n  async function performLogin() {\n    let response;\n    try {\n      response = await fetch('/login', {\n        method: 'POST',\n        body: JSON.stringify({\n          username: document.getElementById('username').value,\n          password: document.getElementById('password').value\n        }),\n        headers: {\n          'Content-type': 'application/json; charset=UTF-8'\n        }\n      });\n    } catch (error) {\n      console.log('There was an error', error);\n    }\n\n    if (response?.ok) {\n      console.log(\"Login response call is successful\");\n      window.location.href = '");
		jteOutput.setContext("script", null);
		jteOutput.writeUserContent(loginPageParams.getRedirectTo());
		jteOutput.writeContent("';\n    } else {\n      console.error(\"Status = \" + response?.status);\n      document.getElementById(\"errors\").innerText = await response?.json();\n    }\n  }\n</script>\n<div>\n    <label for=\"username\">\n        Username:\n    </label>\n    <input type=\"text\"\n           id=\"username\"\n           name=\"username\"\n           placeholder=\"Enter your Username\" required>\n\n    <label for=\"password\">\n        Password:\n    </label>\n    <input type=\"password\"\n           id=\"password\"\n           name=\"password\"\n           placeholder=\"Enter your Password\" required>\n    <div>\n        <button type=\"submit\" onclick=\"performLogin()\">Submit</button>\n    </div>\n    <div id=\"errors\" style=\"color: red\">\n    </div>\n</div>\n");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		LoginPageParams loginPageParams = (LoginPageParams)params.get("loginPageParams");
		render(jteOutput, jteHtmlInterceptor, loginPageParams);
	}
}
