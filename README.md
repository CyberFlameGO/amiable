# Amiable
Amiable is a web app for monitoring the use of AMIs.
You can access it here:
https://amiable.gutools.co.uk


## How to run Amiable locally
Amiable uses Google Auth. For this reason, we need to run Amiable through an nginx proxy `amiable.local.dev-gutools.co.uk`. 
This can be achieved by running:

```shell script
./script/setup
```
 
 - Setup Amiable configuration.
 A conf file is expected from Amiable.
 The location of that file (as shown in `./sbt`) is: `$HOME/.gu/amiable.local.conf`
 That file must contain all the configuration values that exist in `application.conf`
 
Here's a (possibly incomplete) list of properties to set. You'll need to get these properties
from another team member or access to the amiable project on google cloud platform to get them
from there.
 ```
    
GOOGLE_CLIENT_ID=""
GOOGLE_CLIENT_SECRET=""
GOOGLE_SERVICE_ACCOUNT_CERT_PATH="/path/to/cert.json"
GOOGLE_2FA_USER="email@email.com"
2FA_GROUP_ID="email_group@group.com"
AUTH_DOMAIN="guardian.co.uk"

PRISM_URL="https://prism.gutools.co.uk"
HOST="https://amiable.local.dev-gutools.co.uk"
APPLICATION_SECRET="secret"
MAIL_ADDRESS="email@email.com"

include "application"
 ```
 In order to setup the auth parameters (eg. `serviceAccountCertPath`),
 please consult someone from the Dev Tools team.
 
 - `./sbt run` open your browser at `https://amiable.local.dev-gutools.co.uk`!
 
### Common problems
 - If when running master you can an error "Could not find a suitable constructor..." it's something wrong with your
 config file - you probably need to add `include "application.conf"` to your `application.local.conf` file.
