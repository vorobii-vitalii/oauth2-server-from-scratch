@import java.util.stream.Collectors
@import api.security.training.users.login.dto.LoginPageParams

@param LoginPageParams loginPageParams

<h1>Login</h1>
<h3>Enter your login credentials</h3>
<script>
  async function performLogin() {
    let response;
    try {
      response = await fetch('/login', {
        method: 'POST',
        body: JSON.stringify({
          username: document.getElementById('username').value,
          password: document.getElementById('password').value
        }),
        headers: {
          'Content-type': 'application/json; charset=UTF-8'
        }
      });
    } catch (error) {
      console.log('There was an error', error);
    }

    if (response?.ok) {
      console.log("Login response call is successful");
      window.location.href = '${loginPageParams.getRedirectTo()}';
    } else {
      console.error("Status = " + response?.status);
      document.getElementById("errors").innerText = await response?.json();
    }
  }
</script>
<div>
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
    <div id="errors" style="color: red">
    </div>
</div>
