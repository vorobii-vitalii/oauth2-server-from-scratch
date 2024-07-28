@param String clientName
@param String clientDescription
@param java.util.Collection<String> scopeList
@param String authorizationRequestId

<script>
  async function approveRequest() {
    console.log("Going to approve ${authorizationRequestId}");
    try {
      const loginResponse = await fetch('/approve/${authorizationRequestId}', {
        method: 'POST',
        headers: {
          'Content-type': 'application/json; charset=UTF-8'
        }
      });
      const jsonResponse = await loginResponse.json();
      console.log("JSON response");
      console.log(JSON.stringify(jsonResponse));
      window.location.href = jsonResponse.redirectURL;
    }
    catch (err) {
      console.error("Error on approve...");
      console.error(err);
      // TODO: Add error on UI
    }
  }

  function rejectRequest() {
    console.log("Going to reject ${authorizationRequestId}");

  }
</script>

<h1>${clientName}</h1>
<p>${clientDescription}</p>
@if(scopeList.isEmpty())
    <h3>This app is asking for all permissions on your account</h3>
@else
    <h3>This app is asking for following permissions on your account</h3>
    <ul>
        @for(var permission : scopeList)
            <li>${permission}</li>
        @endfor
    </ul>
@endif

<div>
    <button onclick="approveRequest()">Approve</button>
    <button onclick="rejectRequest()">Reject</button>
</div>