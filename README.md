![Build Status](https://img.shields.io/github/workflow/status/CDFN/SkyBlock/Java%20CI%20with%20Gradle?style=flat-square)
![Open Issues](https://img.shields.io/github/issues-raw/CDFN/SkyBlock?style=flat-square)
![Stars](https://img.shields.io/github/stars/CDFN/SkyBlock?style=flat-square)
![License](https://img.shields.io/github/license/CDFN/SkyBlock?style=flat-square)

# SkyBlock 

### What's this project about?
This project initially wasn't about SkyBlock but more about scalable infrastructure for Minecraft network.
I just decided SkyBlock will be great choice to make playable and scalable gamemode.
I use this project as my "playground" for testing cool solutions and pushing myself to limits of design creativity.

### What has been done so far?
Not much has been done yet, but there are few things I'm already proud of. <br>
Here are some of them:
 - Redis messaging backend based on Java hacks (reflections & drop of unsafe). It makes communication between multiple servers easy and adding new messages doesn't require much effort.
 - Player data synchronization between multiple servers (inventory, advancements, statistics). It allows persistence of player's data when he switches from one server to another.
 - Complete test environment (based on Docker). It allows developers to quickly test their changes in consistent, isolated environment. It works out of the box!

### Can I become contributor?
Yes! I'll do my best to keep `Issues` updated. You can open `Issues` tab and choose one of them and simply work on it. 
When you'll be ready, you're more than welcome to create Pull Request with your changes.
If you'd like to review pull requests instead, head to `Pull requests` and tell us what do you think about changes!

<h6>This project is licensed under GNU GPL v3. See: [LICENSE](github.com/CDFN/SkyBlock/blob/master/LICENSE)</h6>