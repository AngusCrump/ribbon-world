# Ribbon World
![screenshot](https://raw.githubusercontent.com/AngusCrump/ribbon-world/refs/heads/main/screenshot.png)
(Image taken with distant horizons (must have "Distance Generation" disabled or it fills in the void chunks))

##
Simple mod inspired by the "Ribbon World" type in Factorio. Does a few things:
 - Removes terrain in the +/- Z direction, limiting the generated terrain to:
   - 384 blocks (24 chunks) wide in the overworld (same as -64 to 320 build height)
   - 256 blocks (16 chunks) wide in the nether/end (same as 0 to 256 build height)
 - Creates a 1-chunk wide wall of  barrier blocks at the border between terrain and void.
 - Stops Nether portals from generating outside of the border in the overworld (shouldn't happen in the nether)
 - Stops End gateways from generating outside the border (they just generate at Z = 0)
 - Tries to stop players from spawning outside the border (may not work if game cannot find valid spawn inside border, e.g. spawn is an ocean 🤷)
