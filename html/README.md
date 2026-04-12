# CalorieTech Blog Scraper

爬取 https://www.calorietech.com/blog/plan 网页内容的脚本

## 安装依赖

```bash
pip install -r requirements.txt
```

## 使用方法

```bash
python3 scraper.py
```

## 输出文件

脚本会生成以下文件：

- **content.json** - 结构化的内容数据（JSON 格式）
- **page.html** - 完整的网页 HTML
- **summary.txt** - 易读的内容摘要

## 功能

- ✅ 爬取网页标题和主要内容
- ✅ 提取文章列表（标题、链接、摘要）
- ✅ 保存为多种格式（JSON、HTML、TXT）
- ✅ 包含错误处理和日志记录
- ✅ 遵守爬虫礼仪（添加延迟、User-Agent）

## 注意事项

- 请遵守网站的 robots.txt 和服务条款
- 脚本包含 1 秒延迟以尊重服务器
- 仅用于学习和个人使用
