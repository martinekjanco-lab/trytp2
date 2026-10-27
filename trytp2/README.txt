TryTP (Fabric, Minecraft 1.21.11, client-side)

/trytp <player>   wait for them to turn TPA on, then send /tpahere <player>
/trytp cancel     stop waiting
/trytp reload     reload config
Config: .minecraft/config/trytp.json (edit "patterns" to match your server's TPA-on message)
Build: push to GitHub, Actions builds the jar (see .github/workflows/build.yml). Needs Fabric API in mods.
