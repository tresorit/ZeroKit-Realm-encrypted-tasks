# End-to-end encrypted infrastructure for mobile apps
This tutorial will guide you through the steps of deployment and configuration of an advanced backend infrastructure which support end-to-end encrypted, reactive mobile apps. At the end you can try it out with an encryption-enabled todo-list app.
The reactive mobile backend is based on Realm Mobile Platform while the encryption is brought to you by Tresorit's ZeroKit platform.

## Infrastructure
An infrastructure for such an advanced systems contains several items and services. For a ZeroKit-Realm based infrastructure consider the following architecture.

![enter image description here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/zerokit-realm-architecture.PNG)

The boxes on the picture are infrastructure items. The green "APP" box is the mobile application itself which consumes the infrastructure backend (represented by the three other boxes). As you can see, the application includes both Realm and ZeroKit mobile SDKs, and communicates with all backend nodes through the internet.

The three other boxes together form the backend infrastructure for the mobile app. You can imagine them as separate services / servers.

The "ROS" labelled one is represents an installed Realm Object Server, which can be deployed anywhere (in the cloud, on your machine, in a VM in your desktop etc.) The only requirement is that it must be accessible by the mobile application and the ROS must access the internet, so it can communicate with the ZeroKit service. As you can see, a ZeroKit authentication module is deployed for the ROS, to enable ZeroKit-based secure authentication. This module needs internet access to reach ZeroKit service. (Communication flows are marked by arrows on the picture).
ROS itself provides the reactive backend and data store for the mobile application.

ZeroKit example server is a small NodeJS application provided by Tresorit. This app can be hosted by you on your machine or in a VM - typically for development, or in the cloud - for production use cases. This app handles all the administrative communication between the app and the ZeroKit service, and also provides a structured data store for the system (mostly for administrative data, as ROS itself is a database).
As you can see, this app needs a backend database. By default, this database is a MongoDB instance, but it can be changed easily by implementing the storage service of it for different data stores.
In this tutorial we will install the sample server into MS Azure Cloud along with the needed MongoDB (DocumentDb) instance.
**Note:** Wherever this server is installed, it must be under the control of the system owner, and must not be accessible for administration by Tresorit or other parties.

The ZeroKit service is a hosted online service provided by Tresorit AG. ZeroKit handles all the management flows silently which are needed to provide a shareable, zero-knowledge, end-to-end encryption platform for your users. You can get a free sandbox account for development at https://manage.tresorit.io by a simple registration. You can configure your ZeroKit sandbox tenant on the same webpage.

## I. Register a ZeroKit tenant
**Info:** You can skip this step if you already has a ZeroKit tenant.

Please navigate to https://manage.tresorit.io and register yourself a free sandbox account. After the first login the system will automatically provision you a free sandbox ZeroKit tenant, which can be used for free for development purposes.

## II. Configure tenant
Please log in to the ZeroKit admin portal at https://manage.tresorit.io. After successful login you will see the main configuration page of your tenant.
On the first few lines you will find the basic configuration values of your tenant: service url, admin user id and administrative keys. You will need these values later during the configuration of the other system components. 

![enter image description here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/zerokit-basic-settings.PNG)

**Warning:** You can come back here anytime to copy these values. Please never copy and store them at any other place.

If you scroll down a bit, you will find a section called "Identity provider". This is where you can configure the built-in [OpenID Connect](https://openid.net/connect/) authentication provider module of the ZeroKit service. In this step we will add a new IDP client configuration which will be used by the mobile apps and the ROS backend for login.

![enter image description here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/zerokit-idp-settings.PNG)

Click on the "Add client" button and complete the form according to the following instructions:

* The name can be your choice, its only used by the portal to identify your client.
* Please add the following redirect URL: "https://*{client_id}*.*{tenant_id}*.api.tresorit.io/", where you substitute *{client_id}* for the id of the actual client, and *{tenant_id}* is the id of your tenant. This special URL format is needed only by the mobile app (SDK) clients.
  **Important:** Notice the necessary slash ("/") at the end of the URL.
* Please set the client flow to "Hybrid"
* Click apply. (It can take 2-5 minutes for the changes to be effective).

![enter image description here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/zerokit-realm-idp-client.png)

## III. Install Realm Object Server (ROS)
**Info:** You can skip this step if you already has a ZeroKit tenant.

Please navigate to https://realm.io/products/realm-mobile-platform/ and follow the instructions according to your platform to install a ROS instance for yourself. Any edition of ROS will do for this setup.

## IV. Configure authentication
To enable ZeroKit based authentication, you have to install the ZeroKit-Realm authentication module for ROS. The module is open-source, you can find it on GitHub: https://github.com/tresorit/ZeroKit-Realm-auth-provider

To install the module for ROS, please open a terminal *on the same machine* where ROS is installed, and run the following command 
```bash
curl -sL https://github.com/tresorit/ZeroKit-Realm-auth-provider/raw/master/install.sh | sudo -E bash -
```
**Notice:** if you need more help or want to install it manually, you can find more detailed description in the module's repository on GitHub

The script will automatically install the module and will produce a code snippet which should be inserted into the configuration file of ROS in the "auth" section.

> Realm Object Server's config file is can be found at this location, according ot your platform:
  >   - On linux: /etc/realm/configuration.yml
  >   - On OsX:   {realm-mobile-platform-directory}/realm-object-server/object-server/configuration.yml

  **Configuration block of ZeroKit auth module for ROS:**
 This is just an example, please use the code snippet produced by the installer as it may contain further modifications.
```yml
auth:
    providers:
       # This enables login via ZeroKit's secure identity provider:
       # The client ID of the IDP client created for the Realm object server
       # on ZeroKit management portal (https://manage.tresori.io)
       client_id: 'example_client'
  
       # The client secret of the IDP client created for the Realm object server
       # on ZeroKit management portal (https://manage.tresori.io)
       client_secret: 'example_secret'
  
       # The service URL of your ZeroKit tenant. It can be found on the main
       # configuration page of your tenant on ZeroKit management portal
       # (https://manage.tresori.io)
       service_url: 'https://example.api.tresorit.io'

      # The include path to use for including ZeroKit auth implementation.
      # Usually it's /usr/local/zerokit/zerokit-realm-auth-provider
      include_path: '/usr/local/zerokit/zerokit-realm-auth-provider'

      # This refers to the actual implementation (should be zerokitauth)
      implementation: 'zerokitauth'
```

After you have copied the snippet into the configuration file, please edit it and change:

* *Service uri* for the service uri of your tenant (you can find the value on the main config page)
* *Client ID* for the id of the IDP client you have configured in the second step of the tutorial
* *Client secret* for the secret string of the same IDP client 

Now you can restart ROS to pick up new config. On MAC simply close and restart the startup script of the server, on Linux please type the following line in a terminal:
```bash
sudo systemctl restart realm-object-server
```

If the server has started successfully (you can access the Realm dashboard in a browser), then you have completed the ROS configuration and you can move to the next section. On a failure you can try to start the server manually from a command line to see the error which prevents it from starting.

## One-click template deployment of sample server
Next step is to install the ZeroKit sample backend server. You can find the source code and the manual install instructions on GitHub, but in this tutorial we will use a one-click Azure deployment to create a new infrastructure in MS Azure Cloud for it. (For this you will need a MS Azure subscription account.)

The repository for the one-clieck installer can be found [here](https://github.com/tresorit/ZeroKit-Azure-backend-sample). if you need any more help with this app, you can find it there.

Please click on this button to proceed with the installation. After you click, the Azure portal will ask for a few settings and then it will deploy a new resource group with a web application (sample server) and an Azure DocumentDB with MongoDB interface. This kind of deployment is scaleable and also suitable or production infrastructures.

<a href="https://portal.azure.com/#create/Microsoft.Template/uri/https%3A%2F%2Fgithub.com%2Ftresorit%2FZeroKit-Azure-backend-sample%2Fraw%2Fmaster%2FZeroKitNodejsSampleDeployment%2Fazuredeploy.json" target="_blank"> <img src="http://azuredeploy.net/deploybutton.png"/></a>

After you have clicked and logged in to Azure portal, you should see this screen:

![enter image description here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/azure-template-deployment.png)

The non-zkit properties and instance sizes are completely your choice. You can change the resource sizes later any time on the Azure portal.

The tenant settings are the settings from the ZeroKit management portal (from the second step). Please copy them along with the IDP client settings created in the same step and proceed the deployment.

After the deployment finished (about 5 minutes), please find and open the configuration page of the web app. In the "Application settings" tab you can change the settings of the server any time.

![enter image description here](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/raw/master/.images/azure-webapp-config.png)

## Configure and build mobile applications
Last step is to check out, configure and build the mobile applications. You can find the platform-specific guide on the following links:

  * **Android:** [https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/tree/master/android](https://github.com/tresorit/ZeroKit-Realm-encrypted-tasks/tree/master/android)

**Note:** Currently only the android version is available, the iOS version is expected to follow in 1-2 weeks.
