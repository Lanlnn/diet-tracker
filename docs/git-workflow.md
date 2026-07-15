# Git 与 GitHub 交付规范

## 1. 唯一账号与仓库

- GitHub 账号：`Lanlnn`
- 远端仓库：`https://github.com/Lanlnn/diet-tracker.git`
- 默认分支：`master`
- 功能开发不得直接推送到 `master`，统一通过草稿 PR 验证和合并。

每次开始工作前检查：

```bash
gh auth status
git remote -v
git status --short --branch
```

期望看到登录账号为 `Lanlnn`，`origin` 的 fetch/push 地址都属于 `Lanlnn/diet-tracker`。

## 2. 本仓库提交身份

本仓库使用 GitHub 隐私邮箱，避免把私人邮箱写入公开提交：

```bash
git config --local user.name Lanlnn
git config --local user.email 96610980+Lanlnn@users.noreply.github.com
```

这是仓库级配置，不会影响电脑上的其他 Git 项目。

## 3. M0–M10 分支规则

每个阶段使用独立分支：

```text
codex/m0-foundation
codex/m1-design-system-shell
codex/m2-auth-profile
...
codex/m10-profile-release
```

开始新阶段：

```bash
git switch master
git pull --ff-only origin master
git switch -c codex/mN-short-name
```

只有当前阶段的代码、测试、文档和必要设计资源可以进入该分支。旧设计散图、ZIP、DOCX、密钥、IDE 私有配置和构建产物不得提交。

## 4. 提交和推送

提交前依次执行：

```bash
git status --short
git diff
git diff --check
```

只暂存本阶段文件，不使用不加检查的 `git add -A`：

```bash
git add path/to/file
git diff --cached --check
git diff --cached
git commit -m "feat: 完成清晰且单一的改动"
git push -u origin "$(git branch --show-current)"
```

推荐提交前缀：`feat`、`fix`、`refactor`、`test`、`docs`、`chore`。

## 5. PR 与阶段完成

首次推送后创建草稿 PR，目标分支固定为 `master`。PR 至少包含：

- 本阶段完成内容和未完成内容
- UI、API、数据库变更
- 自动化检查和真机验证结果
- 风险、迁移与回滚说明
- 对应里程碑验收清单

CI 全部通过且里程碑验收完成后，才把草稿改为 Ready for review 并合并。合并后创建阶段标签：

```bash
git switch master
git pull --ff-only origin master
git tag -a mN-complete -m "M<N> complete"
git push origin mN-complete
```

标签只表示该阶段真实完成，不能在计划阶段提前创建。

## 6. 推送异常处理

### 远端账号错误

先停止推送，不要通过添加无关协作者绕过：

```bash
git remote set-url origin https://github.com/Lanlnn/diet-tracker.git
```

### 非快进错误

先获取远端并变基自己的提交：

```bash
git fetch origin
git rebase origin/当前分支名
```

不要使用 `git push --force`。只有改写自己尚未合并的分支历史时，才允许使用 `git push --force-with-lease`。

### 工作区混入无关文件

不要删除来源不明的用户文件。先检查文件归属，只暂存本阶段路径；本地设计归档通过 `.gitignore` 排除。
