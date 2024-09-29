#!/bin/bash

# 通过脚本吧项目中的所有地址相关内容改成192.168.0.1
project_dir="."

# 查找所有 yml 文件
find "$project_dir" -type f -name "*.yml" | while read -r file; do
  # 使用 sed 替换 IP 地址
  sed -i '' 's/[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}/192.168.0.1/g' "$file"
  sed -i '' 's/r-[a-zA-Z0-9.-]*\.redis\.rds\.aliyuncs\.com/192.168.0.1/g' "$file"
  sed -i '' 's/rm-[a-zA-Z0-9.-]*\.mysql\.rds\.aliyuncs\.com/192.168.0.1/g' "$file"
  echo "Processed $file"
done

echo "IP addresses have been replaced."