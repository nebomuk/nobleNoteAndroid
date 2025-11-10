# nobleNoteAndroid

**nobleNote for Android** is a simple note-taking app.

Notes can be organized into notebooks, and each note is saved as an HTML file on the SD card or internal storage.  
It is compatible with the [nobleNote](https://github.com/hakaishi/nobleNote) desktop application for Linux, Windows, and macOS.  
The integrated text editor offers limited rich-text formatting options when text is selected, while the nobleNote desktop application provides a full-featured rich-text editor.

The app requires **Android 7 or higher** to run.

---

## Sync notes between desktop and mobile devices

1. Install [nobleNote](https://github.com/hakaishi/nobleNote) on your Linux, Windows, or macOS device and locate the folder containing the nobleNote notebooks.

2. In **nobleNoteAndroid**, select a folder on the external storage (SD card) using the settings.

3. Use a sync tool of your choice (e.g., [Syncthing](https://syncthing.net) or Dropbox) to synchronize the folder containing the notebooks with the same folder on your Linux, Windows, or macOS device.  
   nobleNote will automatically detect changes in the file system and reload notes as needed.

---

## Build from source

The source code can be built using **Android Studio Otter or later**.

---

## Screenshots

<img src="/screenshot/Screenshot0.png" width="200px">  
<img src="/screenshot/Screenshot1.png" width="200px">  
<img src="/screenshot/Screenshot2.png" width="200px">  
<img src="/screenshot/Screenshot3.png" width="200px">  

<img src="/screenshot/ScreenshotTablet2.png" width="300px">

---

## License

nobleNoteAndroid is licensed under the MIT License.

(C) 2025 Fabian Deuchler

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.