#!/bin/bash

# 모든 Java 클래스 파일에서 클래스명을 추출
cd "/c/Work/git/cson/src/main/java/com/hancomins/json5/serializer"

echo "=== 사용되지 않는 클래스 검사 ==="

# 각 클래스가 다른 파일에서 참조되는지 확인
for file in *.java; do
    if [ "$file" != "*.java" ]; then
        classname=$(basename "$file" .java)
        
        # 자기 자신 파일을 제외하고 다른 파일에서 참조되는지 확인
        references=$(grep -l "$classname" *.java | grep -v "$file" | wc -l)
        
        if [ $references -eq 0 ]; then
            echo "❌ 사용되지 않는 클래스: $classname"
        else
            echo "✅ 사용되는 클래스: $classname (참조 수: $references)"
        fi
    fi
done
