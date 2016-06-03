#App Sandbox

##Checklist

- [x] assets - AssetManager#addAssetPath
- [x] classes.dex - DexClassLoader
- [ ] resources
- [ ] AndroidManifest.xml

##Not supported

- [x] Apk extension files (obb)
- [x] Native libraries (*.so)
- [x] custom permissions cannot work because all permissions must be predeclared in android manifest

##Drawbacks

- App must request all possible permissions