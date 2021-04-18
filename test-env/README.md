Steps to run dev environment:

- Change content of `velocity-config/velocity_secret` file to anything else
- Execute following commands to make hard link for ease of debug:
    - `ln ../skyblock-bukkit/build/libs/skyblock-bukkit-1.0.0-all.jar data/lobby/plugins/skyblock.jar`
    - `ln ../skyblock-bukkit/build/libs/skyblock-bukkit-1.0.0-all.jar data/mc-1/plugins/skyblock.jar`
    - `ln ../skyblock-bukkit/build/libs/skyblock-bukkit-1.0.0-all.jar data/mc-2/plugins/skyblock.jar`
    - `ln ../skyblock-velocity/build/libs/skyblock-velocity-1.0.0-all.jar data/velocity/plugins/skyblock.jar`
- Run `docker-compose up -d` and wait until everything starts up
- Connect to server via `localhost:25565` 