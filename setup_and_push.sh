#!/usr/bin/env bash
# =============================================================================
# Dersium — GitHub Push + Build Script
# Çalıştır: chmod +x setup_and_push.sh && ./setup_and_push.sh
# =============================================================================
set -euo pipefail

# ── Renkler ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'
BOLD='\033[1m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✅ $*${NC}"; }
info() { echo -e "${BLUE}ℹ️  $*${NC}"; }
warn() { echo -e "${YELLOW}⚠️  $*${NC}"; }
err()  { echo -e "${RED}❌ $*${NC}"; exit 1; }

echo -e "${BOLD}=== Dersium — GitHub + Build Setup ===${NC}\n"

# ── 1. Gereksinim kontrolleri ─────────────────────────────────────────────────
info "Gereksinimler kontrol ediliyor..."
command -v git  >/dev/null || err "git bulunamadı. Lütfen git yükleyin."
command -v java >/dev/null || err "java bulunamadı. JDK 17+ gerekli."

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
[[ "$JAVA_VER" -lt 17 ]] && err "JDK 17+ gerekli. Mevcut: $JAVA_VER"
ok "Java $JAVA_VER OK"

# ANDROID_HOME kontrolü
if [[ -z "${ANDROID_HOME:-}" ]]; then
  # Yaygın konumları dene
  for candidate in \
    "$HOME/Android/Sdk" \
    "$HOME/Library/Android/sdk" \
    "/opt/android-sdk" \
    "C:/Users/$USER/AppData/Local/Android/Sdk"; do
    if [[ -d "$candidate" ]]; then
      export ANDROID_HOME="$candidate"
      break
    fi
  done
fi

if [[ -z "${ANDROID_HOME:-}" ]]; then
  warn "ANDROID_HOME bulunamadı!"
  echo "  Android Studio > SDK Manager > SDK Location'dan kopyalayın."
  echo "  Örnek: export ANDROID_HOME=\$HOME/Android/Sdk"
  echo "  Sonra bu scripti tekrar çalıştırın."
  exit 1
fi
ok "ANDROID_HOME: $ANDROID_HOME"

# ── 2. local.properties ───────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOCAL_PROPS="$SCRIPT_DIR/local.properties"
if [[ ! -f "$LOCAL_PROPS" ]]; then
  echo "sdk.dir=$ANDROID_HOME" > "$LOCAL_PROPS"
  ok "local.properties oluşturuldu"
else
  info "local.properties zaten var"
fi

# ── 3. Gradle wrapper ─────────────────────────────────────────────────────────
GRADLEW="$SCRIPT_DIR/gradlew"
if [[ ! -f "$GRADLEW" ]]; then
  info "gradlew bulunamadı, indiriliyor..."
  command -v gradle >/dev/null || err "gradle bulunamadı. Gradle veya Android Studio gerekli."
  cd "$SCRIPT_DIR" && gradle wrapper --gradle-version=9.4.1
  ok "Gradle wrapper oluşturuldu"
fi
chmod +x "$GRADLEW"

# ── 4. Git repository ─────────────────────────────────────────────────────────
cd "$SCRIPT_DIR"
if [[ ! -d ".git" ]]; then
  info "Git repo başlatılıyor..."
  git init
  git checkout -b main
  ok "Git repo başlatıldı"
fi

# .gitignore
cat > .gitignore << 'GITIGNORE'
# Gradle
.gradle/
build/
**/build/
local.properties
*.iml

# Android
*.ap_
*.dex
*.class
proguard/
bin/

# IDE
.idea/
*.iws
*.ipr
*.DS_Store
Thumbs.db

# Keys
*.jks
*.keystore
keystore.properties

# Room schemas (opsiyonel — CI için tutabilirsin)
# **/schemas/

# Test outputs
**/test-results/
**/jacoco/
GITIGNORE
ok ".gitignore oluşturuldu"

# ── 5. GitHub'a push ──────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}GitHub Repository Ayarları${NC}"
echo "GitHub'da önce boş bir repository oluşturun:"
echo "  https://github.com/new"
echo ""
read -rp "GitHub repository URL (örn: https://github.com/kullanici/dersium.git): " REPO_URL

if [[ -z "$REPO_URL" ]]; then
  warn "Repository URL girilmedi, GitHub push atlanıyor."
else
  git add -A
  git commit -m "feat: Initial Dersium project — Android SDK 36, Gradle 9.4.1

- 16-module Clean Architecture (MVVM)
- Jetpack Compose + Material 3
- Room 2.7.1 + Hilt 2.55 + KSP
- DataStore + WorkManager
- Biometric auth (PIN + Face/Fingerprint)
- Students, Lessons, Calendar, Financial, Reports, Settings modules
- Dark mode with Indigo/Green/Red accent palette
- Turkish UI"

  if git remote get-url origin 2>/dev/null; then
    git remote set-url origin "$REPO_URL"
  else
    git remote add origin "$REPO_URL"
  fi

  echo "GitHub kullanıcı adı ve token gerekebilir."
  git push -u origin main && ok "GitHub'a başarıyla push edildi! 🎉" || \
    warn "Push başarısız. Token doğru mu? GitHub > Settings > Developer Settings > Personal Access Tokens"
fi

# ── 6. Debug build ────────────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}Debug Build${NC}"
read -rp "Debug build başlasın mı? [y/N]: " BUILD_NOW

if [[ "${BUILD_NOW,,}" == "y" ]]; then
  info "Debug APK derleniyor... (ilk seferde ~5-10 dakika sürebilir)"
  cd "$SCRIPT_DIR"
  ./gradlew assembleDebug \
    --stacktrace \
    --build-cache \
    --parallel \
    -Porg.gradle.jvmargs="-Xmx4g -XX:+UseParallelGC" \
    2>&1 | tee build_debug.log

  APK_PATH=$(find . -name "*.apk" -path "*/debug/*" | head -1)
  if [[ -n "$APK_PATH" ]]; then
    ok "APK hazır: $APK_PATH"
    ls -lh "$APK_PATH"
  else
    err "APK oluşturulamadı. build_debug.log dosyasını inceleyin."
  fi
fi

echo ""
ok "İşlem tamamlandı!"
