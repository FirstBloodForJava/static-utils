~~~bash
# 当前git项目配置代理
git config --local http.proxy http://127.0.0.1:33210

git config --local https.proxy https://127.0.0.1:33210

~~~



~~~bash
# 本地初始化git项目步骤
echo "# test" >> README.md
git init
# 添加新文件到本地仓库
git add README.md
# commit
git commit -m "first commit"
# 将当前分支名修改为main
git branch -M main
# git branch -m <old-branch-name> <new-name>
# 设置git远程参考地址
git remote add origin <remote addr>
# 和远程参考建立连接
git push -u origin main

~~~

