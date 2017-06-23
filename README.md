# E2EE Realm Backend Setup
Follow these instructions to set up your End-to-End Encrypted Realm solution's backend components. 

## Architecture Overview
The solution consists of 3 components:

1. **Realm Object Server (ROS)**: stores realms and provides realtime sync of realms between ROS and mobile devices.
2. **ZeroKit cloud account**: a ZeroKit cloud tenant authenticates your users with ROS and stores their (encrypted) encryption keys;
3. **ZeroKit backend server**: this server will run your user account verification code for newly registered users. To keep the demo simple, we won't validate your users for now: they'll be auto-validated if they sign up with a username that starts with a test-user- prefix, such as test-user-Alice

On the other end of the wire, your mobile apps will be using Realm's and ZeroKit's mobile SDKs to sync realms and seamlessly end-to-end encrypt data.

<img src="https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/zerokit-realm-architecture.PNG">

## Flow overview
To begin, you'll need a Mac or Linux to host the backend components.

1. You'll start by signing up for a free ZeroKit account and prepare it to work with Realm.
2. If you don't have Realm Object Server (ROS) installed already, you'll need to install it. If you have one, update it to the latest version.
3. You'll then turn on ZeroKit's ultra-secure auth in ROS. This step is required for ZeroKit to work and ensures that your user auth is as secure as your E2EE Realm is. Otherwise, hackers will hack your users' accounts and breach passwords to hack your Realm that way.
4. Finally, you'll install the ZeroKit NodeJS backend. You can later enter your user account validation code (email or phone number validation for example) into this NodeJS app. 

And now, here's the step-by-step instructions for each step:

## 1. Sign up for ZeroKit and prepare it to work with Realm
Go to https://manage.tresorit.io and get yourself a free account.

  Check your email for the confirmation email to verify your email address, then log in to your new account at https://manage.tresorit.io.

This is what you'll see: note below where your ZeroKit tenant's Service URL, Admin user ID and Admin keys are on the portal, you will need these values later when configuring the products.

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/2Portal.png">

Later, you'll need your tenant's admin key. This is how you can copy it to the clipboard: click on the Load primary Admin key then to the Copy button:

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/3Secret.png">

Scroll down and find the Identity provider section.

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/4IDP.png">

**Click Add SDK client**:

* Name the client as you'd like
* Click Apply

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/5New%20IDP%20client.png" width="60%">

Once applied, a new client will show up on the portal:

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/6Client%20added.png">

We're done with this step! Tap yourself on the shoulder.

## 2. Install ROS
**Don't have ROS? Get the latest release here**: https://realm.io/docs/realm-object-server/#install-realm-object-server
When finished, skip to Step 3

**Already have ROS?** Check its version by browsing to the Realm dashboard: by default it's http://localhost:9080

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/7RealmVersion.png" width="80%">

* 1.6.1 or higher? You're good.
* Below 1.6.1? you need to upgrade as your version won't support custom authentication providers and won't work with ZeroKit. Upgrade at https://realm.io/docs/realm-object-server/#upgrading

## 3. Turn on ZeroKit's ultra-secure auth in ROS
**First, install the latest Node.js** from https://nodejs.org. If you have one already, upgrade to the current version.

**Now, install the ZeroKit auth module for ROS** by opening a terminal on the machine where ROS is installed, and running the following command:

```bash
curl -sL https://github.com/tresorit/ZeroKit-Realm-auth-provider/raw/master/install.sh | sudo -E bash -
```

The script will install the auth module and produce a code snippet to the console which you'll need to copy & paste into Realm's configuration.yml config file.
By default, you can find **configuration.yml**:
* On linux: /etc/realm
* On OsX: {realm-mobile-platform-directory}/realm-object-server/object-server/

**Note**: Your current Realm config file probably already contains the "auth:" and "providers:" sections: don't duplicate these, copy only the contents from under "providers:"
Use your favorite Linux text editor (if you don't have one, check out vim or nano). Don't forget to start the command with sudo to start the editor as a root!

```yml
auth:
    providers:

#Copy it from here
custom/zerokit:

    # Grab the following 3 values from your ZeroKit management portal (https://manage.tresori.io):
    client_id: 'abcd1234_efgh5678'

    # Copy the Client Secret field's value here:
    client_secret: 'abcd1234_IjKL9012MNoP'

    # Copy here the service URL from your ZeroKit portal
    service_url: 'https://zkrandomid.api.tresorit.io'

    # Leave it as-is
    include_path: '/usr/local/zerokit/zerokit-realm-auth-provider'

    # Leave it as-is
    implementation: 'zerokitauth'
```

**Replace**: **client_id**, **client_secret** and **service_url** in configuration.yml with the values on the ZeroKit portal at https://manage.tresorit.io:

Now, open up the realm client you created previously on the ZeroKit admin portal:

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/8Client.png">

You can grab the Client ID and Client Secret values from here:

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/9ClientEdit.png" width="60%">

Cancel this window and grab the Service URL from the top of the main page:

<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/10Portal.png">

Now, restart ROS. On a Mac, just close and restart the ROS command, on Linux type:

```bash
sudo systemctl stop realm-object-server
sudo systemctl start realm-object-server
```

To check if ROS started up successfully, **browse to port 9080**.
If the page doesn't load, you probably made a typo in the config file? If it's not the config file, check the console for hints (Mac) or /var/log/realm-object-server.log (Linux).

**If this is a new ROS, register an admin user** on the Realm dashboard in your browser on port 9080.

If you're past this, you deserve one more tap on the shoulder, we're almost there...

## 4. And finally: deploy the ZeroKit NodeJS backend!

**Download** the ZeroKit backend repo from https://github.com/tresorit/ZeroKit-NodeJs-backend-sample-realm

 Or even simpler, **clone it** using the git client:
 
 ```bash
 git clone https://github.com/tresorit/ZeroKit-NodeJs-backend-sample-realm.git
 ```

 **Then, edit config.json - see Step 1 above for screenshots** of where to copy the values from:
 
 ```yml
 {
    "baseUrl": "Your server's Url, no port needed. If you're on localhost, use http://10.0.0.2 (due to Android emulator limitation)",
    "appOrigins": [],
    "zeroKit": {
      "serviceUrl": "Replace with the Service URL from the ZeroKit portal",
      "adminUserId": "Replace with the Admin user ID from the ZeroKit portal",
      "adminKey": "Replace with the Primary Admin Key from the ZeroKit portal",
      "sdkVersion": "4",
      "idp": [
        {
          "clientID": "Replace with the client id of the new client you created on the ZeroKit portal",
          "clientSecret": "Replace with the client secret",
          "callbackURL": "Replace with the https:// URL from below your client secret"
        }
      ]
    }
  }
  ```
  
When you saved the config file, open a console and cd into the root of the NodeJS repo

* First, install it:

  ```bash
  npm install
  ```

* Then run it:

  ```bash
  npm start
  ```

**Test it by browsing to port 3000** of your server where you should see a status message.
If it gives you a 404/NOT FOUND or an error, look at the console for cues on the issue.

Tadam! Your E2EE Realm backend (sandbox) is ready.
**Note**: this is not a production-ready E2EE Realm deployment, but it's perfect for testing. Check the last section of this document for a live-production checklist.


## Next up: test drive the E2EE RealmTasks sample app
<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/11iOS%20logo.png" width="100">
<img src="https://github.com/davidszabo26/RealmTempImgs/blob/master/.images/12Android%20logo.png" width="100">

E2EE Realm Tasks app for iOS and Android: [link here]

Or start from Scratch (iOS and Android): [link here]

## Live production checklist

Although your E2EE (sandbox) environment is now ready for play, there are a few things left before you take it to live production:

1. **Implement user account validation**: in the sandbox, newly registered users (with a test-user- prefix) are auto-approved. This way, you've no control over who signs up for your app and whether their email address/phone numbers are valid: it's messy and unsecure. Add your own user validation code to be called following user registration in the finishedRegistration function in app.js of the ZeroKit node backend you deployed in Step 4.
2. **Set up HTTPS**: get certs and set up HTTPS for your Realm and ZeroKit backends. Even though everything goes end-to-end encrypted, it's important to add an additional layer of security at transport.
3. **Get ready to go live with ZeroKit**: sign up for a production ZeroKit tenant, click through the security checklist and learn about the legal/export requirements of publishing encrypted apps in the app stores. Getting your app's export license approved by the Bureau of Industry and Security can take over 2 months, so it's best if you start that process right now. Check out the ZeroKit go-live checklist for guidance: [link]
4. **Large-scale**: large E2EE Realm deployments are best with Realm's Professional or Enterprise Editions and with a production scalable ZeroKit deployment. Learn more about Realm editions at https://realm.io/pricing. Check out a Scalable Azure-deployable ZeroKit backend installation at https://github.com/tresorit/ZeroKit-Azure-backend-sample
