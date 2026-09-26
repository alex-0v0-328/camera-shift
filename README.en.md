# Camera Shift

[简体中文](README.md) | English

A small client-side patch for Epic Fight: first person in mining mode, third person (back) in battle mode, with a smooth camera glide between them.

This is a translation of [README.md](README.md); where the two differ, the Chinese version prevails.

## Requirements

|                    |                                        |
|--------------------|----------------------------------------|
| Minecraft          | `1.21.1`                               |
| NeoForge           | `21.1.238` or newer                    |
| Java               | `21`                                   |
| mod id / package   | `camerashift` · `net.alex.camerashift` |
| Required           | Epic Fight `21.17.3.1` or newer        |
| Optional compat    | Better Lock On                         |
| Side               | Client only; servers don't need it     |

Exact versions live in `gradle.properties`.

## Behavior

- Entering battle mode switches to the third-person back view, and the camera glides out from behind the head to its normal distance.
- Returning to mining mode glides the camera in behind the head, then drops into first person.
- Switching again mid-glide sends the camera back from where it is; pressing F5 mid-glide keeps the player's choice.
- Joining a world aligns the view with the current mode without a glide.
- On start it turns off Epic Fight's own Auto Perspective Switching and saves that to Epic Fight's config, since this mod drives the perspective instead.

## Configuration

`config/camerashift-client.toml`:

| Key                 | Default | Meaning                                                  |
|---------------------|---------|----------------------------------------------------------|
| `transitionSeconds` | `0.4`   | Glide duration in seconds, `0`–`2`; `0` switches instantly |

## Compatibility

- **Better Lock On**: its switch to first person when the camera gets close (`autoSwitchFirstPerson`) pauses during a glide and works as usual otherwise; without it installed, this compat code does not load.
- **Epic Fight's shoulder camera**: with `TPS activation` set to `Always`, Epic Fight owns the third-person camera and this mod falls back to instant switching; the default `on Aiming` is unaffected.
- **Epic Fight updates**: if a new version changes the internals this mod uses, the mod turns itself off and logs an error while the game keeps running.

## Build and run

```text
gradlew.bat build      # compile, package, run the unit tests
gradlew.bat runClient  # development client
```

Use `./gradlew` on other systems. Epic Fight comes from the Modrinth Maven, so no jar needs to be placed by hand; output lands in `build/libs/`.

## License

The mod itself is copyrighted, all rights reserved. `TEMPLATE_LICENSE.txt` is the MIT license inherited from the NeoForge MDK template and **does not cover the mod's code**.
