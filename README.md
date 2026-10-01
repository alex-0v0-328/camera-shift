# Camera Shift

简体中文 | [English](README.en.md)

Epic Fight 的客户端小补丁：挖掘模式使用第一人称，战斗模式使用第三人称（背后），切换时相机平滑推拉。

## 环境与依赖

|               |                                        |
|---------------|----------------------------------------|
| Minecraft     | `1.21.1`                               |
| NeoForge      | `21.1.252` 及以上                      |
| Java          | `21`                                   |
| mod id / 包名 | `camerashift` · `net.alex.camerashift` |
| 必需依赖      | Epic Fight `21.17.3.1` 及以上          |
| 可选兼容      | Better Lock On                         |
| 运行端        | 仅客户端，服务器无需安装               |

精确版本以 `gradle.properties` 为准。

## 行为

- 切到战斗模式：视角切为第三人称（背后），相机从脑后平滑拉远到正常距离。
- 切回挖掘模式：相机平滑推近到脑后，再进入第一人称。
- 推拉途中再次切换，相机从当前位置原路返回；途中按 F5 以玩家的选择为准。
- 进入世界时视角直接对齐当前模式，不播放推拉。
- 启动后关闭 Epic Fight 自带的「自动切换视角」并写回其配置，改由本模组接管视角切换。

## 配置

`config/camerashift-client.toml`：

| 键                  | 默认  | 说明                                         |
|---------------------|-------|----------------------------------------------|
| `transitionSeconds` | `0.4` | 推拉时长（秒），范围 `0`–`2`；`0` 为瞬间切换 |

## 兼容性

- **Better Lock On**：推拉期间暂停它的相机贴近自动切第一人称（`autoSwitchFirstPerson`），其余时间照常工作；未安装时不加载这部分兼容代码。
- **Epic Fight 越肩视角**：`TPS activation` 设为 `Always` 时，第三人称相机由 Epic Fight 接管，本模组退化为瞬间切换；默认的 `on Aiming` 不受影响。
- **Epic Fight 更新**：若新版改动了本模组用到的内部接口，本模组自动停用并在日志中报错，游戏照常运行。

## 构建与运行

```text
gradlew.bat build      # 编译、打包、运行单元测试
gradlew.bat runClient  # 开发客户端
```

其他系统用 `./gradlew`。Epic Fight 从 Modrinth Maven 获取，无需手动放入 jar；产物位于 `build/libs/`。

## 许可

模组本体版权所有，保留所有权利。`TEMPLATE_LICENSE.txt` 是继承自 NeoForge MDK 模板的 MIT 协议，**不覆盖模组代码**。
