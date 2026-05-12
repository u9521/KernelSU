package me.weishu.kernelsu.ui.playground.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import me.weishu.kernelsu.ui.component.dialog.ConfirmDialogBreeze
import me.weishu.kernelsu.ui.component.dialog.ConfirmDialogVisuals
import me.weishu.kernelsu.ui.component.dialog.LoadingDialogBreeze
import me.weishu.kernelsu.ui.theme.LocalEnableBlur
import me.weishu.kernelsu.ui.util.BlurController
import me.weishu.kernelsu.ui.util.LocalBlurController
import me.weishu.kernelsu.ui.util.blurOverlay
import kotlin.time.Duration.Companion.milliseconds

@Parcelize
private data class MockConfirmVisuals(
    override val title: String,
    override val content: String?,
    override val isMarkdown: Boolean,
    override val isHtml: Boolean,
    override val confirm: String?,
    override val dismiss: String?,
) : ConfirmDialogVisuals

private const val markdownText = $$"""
# Markdown 完整语法测试文档
这是一个用于测试 Markdown 渲染效果的文档。

---

## 1. 标题层级

# 一级标题
## 二级标题
### 三级标题
#### 四级标题
##### 五级标题
###### 六级标题

---

## 2. 文本样式

普通文本 **加粗** *斜体* ***加粗斜体*** ~~删除线~~ `行内代码`
<u>下划线（HTML 标签）</u> 上标<sup>TM</sup> 下标<sub>2</sub>

---

## 3. 列表

### 无序列表
- 苹果
- 香蕉
- 樱桃
  - 子项：车厘子
  - 子项：黑樱桃

### 有序列表
1. 打开冰箱
2. 放入大象
3. 关上冰箱

### 任务列表
- [x] 已完成任务
- [ ] 未完成任务
- [ ] 待办事项

---

## 4. 引用块

> 这是第一级引用
> 可以跨行使用
>
> > 这是嵌套引用
> >
> > > 第三级引用

---

## 5. 代码

### 行内代码
使用 `printf("Hello World\n");` 输出。

### 代码块（带语言标识）
```python
def greet(name):
    print(f"Hello, {name}!")

greet("Markdown")
```

### 代码块（无语言标识，紧凑）
```
$ ls -la
drwxr-xr-x  5 user  staff  160 Apr 10 10:00 .
```

---

## 6. 表格

| 左对齐 | 居中对齐 | 右对齐 |
| :----- | :------: | -----: |
| 苹果   |   红色   |    5元 |
| 香蕉   |  黄色   |    3元 |
| 樱桃   |  暗红   |   12元 |

简单表格（不指定对齐）：

| 姓名 | 年龄 | 城市 |
|------|------|------|
| 张三 | 25   | 北京 |
| 李四 | 30   | 上海 |

---

## 7. 链接与图片

### 链接
[GitHub](https://github.com)
[带提示的链接](https://example.com "鼠标悬停提示")
自动链接：<https://www.markdownguide.org>

### 图片
![Markdown Logo](https://picsum.photos/400/400?random=1 "测试图片")

---

## 8. 水平线与分割线

三个或更多个 `-` `*` `_` 均可：

---

***

___

---

## 9. 脚注

这是一段文字需要脚注[^1]，另一处也需要[^2]。

[^1]: 这是脚注1的内容，详细解释。
[^2]: 脚注2的内容，可以包含 **格式** 和 `代码`。

---

## 10. 数学公式（需渲染器支持，如 LaTeX）

行内公式：$E = mc^2$
独立公式：

$$
\sum_{i=1}^{n} i^2 = \frac{n(n+1)(2n+1)}{6}
$$

---

## 11. 表情符号（Emoji）

:smile: :heart: :+1: :rocket: :fire: :warning:

---

## 12. 定义列表

Markdown
:   一种轻量级标记语言，由 John Gruber 创造。

HTML
:   超文本标记语言，用于构建网页结构。

---

## 13. 标记（Highlight / Mark）

使用 `==高亮==` 需要渲染器支持：==这是高亮文本==

---

## 14. HTML 内嵌

可以使用部分 HTML 标签，例如：

<details>
  <summary>点击展开详情</summary>
  这里是隐藏的内容，可以包含 **Markdown** 格式。
</details>

<kbd>Ctrl</kbd> + <kbd>C</kbd> 复制

---

## 15. 转义字符

特殊符号前加反斜杠：\*不斜体\* \_不斜体\_ \`不代码\` \\反斜杠

---

## 16. 缩进与换行

段落内的换行需要在行尾加两个空格
或者使用 `<br>` 标签。<br>这样就换行了。

"""

@Composable
fun DialogBreezePreview() {
    val showLoading = remember { mutableStateOf(false) }
    val showConfirm = remember { mutableStateOf(false) }

    LaunchedEffect(showLoading.value) {
        if (!showLoading.value) return@LaunchedEffect
        delay(5000.milliseconds)
        showLoading.value = false
    }

    val visuals = remember {
        MockConfirmVisuals(
            title = "test markdown",
            content = markdownText,
            isMarkdown = true,
            isHtml = false,
            confirm = "Delete",
            dismiss = "Cancel"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .blurOverlay()
            .padding(16.dp)
    ) {
        Text(
            text = "DialogBreeze — Loading & Confirm Dialogs",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(onClick = { showLoading.value = true }) {
            Text("Show Loading Dialog")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { showConfirm.value = true }) {
            Text("Show Confirm Dialog")
        }

        LoadingDialogBreeze(showDialog = showLoading)

        ConfirmDialogBreeze(
            visuals = visuals,
            confirm = { showConfirm.value = false },
            dismiss = { showConfirm.value = false },
            showDialog = showConfirm
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDialogBreeze() {
    val blurController = BlurController()
    CompositionLocalProvider(LocalBlurController provides blurController, LocalEnableBlur provides true) {
        DialogBreezePreview()
    }
}
