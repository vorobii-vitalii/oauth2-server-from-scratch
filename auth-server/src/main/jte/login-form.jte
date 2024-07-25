@import java.util.stream.Collectors
@import api.security.training.users.login.dto.LoginPageParams

@param LoginPageParams loginPageParams

<h1>Login</h1>
<h3>Enter your login credentials</h3>
<script>
  async function performLogin() {
    try {
      const loginResponse = await fetch('/login', {
        method: 'POST',
        body: JSON.stringify({
          username: document.getElementById('username').value,
          password: document.getElementById('password').value,
          redirectToOnSuccess: "${loginPageParams.getRedirectTo()}"
        }),
        headers: {
          'Content-type': 'application/json; charset=UTF-8'
        }
      });
      console.log("Login response call is successful");
      console.log(loginResponse);
    }
    catch (err) {
      console.error("Error on Authorize");
      console.error(err);
    }
  }
</script>
<form>
    <label for="username">
        Username:
    </label>
    <input type="text"
           id="username"
           name="username"
           placeholder="Enter your Username" required>

    <label for="password">
        Password:
    </label>
    <input type="password"
           id="password"
           name="password"
           placeholder="Enter your Password" required>
    <div>
        <button type="submit" onclick="performLogin()">Submit</button>
    </div>
</form>
