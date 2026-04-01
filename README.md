# Camellia
Simple config library for fabric mods, no need to install as a separate mod!<br>
Based off of [RoseGarden](https://github.com/Rosewood-Development/RoseGarden) for vanilla servers.

## Features
- Reads and writes settings to a json file
- Functional settings GUI with multiple input types
  - Checkbox
  - Number Input
  - Slider
  - Text Input
  - Combobox
  - Color
  - Lists of all the above
  - Tabs
  - Sections
- Server and client syncing
  - Hides server settings if not OP
  - Sends settings to OPs
  - Modified settings sent back to the server
  - Client-side only settings
<br>

<img width="1344" height="637" alt="image" src="https://github.com/user-attachments/assets/a770f714-dc68-4e15-96de-b727545595e0" />
<img width="1343" height="641" alt="image" src="https://github.com/user-attachments/assets/86adc7b4-7dbb-4dd4-99be-68202e33fe62" />
<img width="1346" height="641" alt="image" src="https://github.com/user-attachments/assets/6a5a9b83-4f87-4088-a0df-90ad3a410f88" />
<img width="1345" height="643" alt="image" src="https://github.com/user-attachments/assets/bda351ad-0ab5-4bc7-829c-52dbde15a4b5" />


## Usage
```java
repositories {
    maven {
        url = "https://repo.rosewooddev.io/repository/public/"
    }
}

dependencies {
    implementation "me.lilac:camellia-fabric:${project.camellia_version}"
}
```

Example files are provided in the repo.<br>
Visit the [wiki](https://github.com/Vekhove/Camellia/wiki) for more info!
