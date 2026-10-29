NoiosoAI 🌌

A privacy-focused Android AI chat app for your local Ollama server.

NoiosoAI lets you connect your Android phone to your own Ollama server and chat with locally hosted language models.

Status: Beta — The app is still in development, so some bugs and issues may occur.
Version 2.0.0 — Coming Soon

I'm working on a new update with more features and improvements. Version 2.0.0 is currently a work in progress.

Features
Local AI chat using Ollama.
Privacy-focused connection to your own server.
Modern Material Expressive-inspired UI built with Jetpack Compose.
Animated backgrounds and a dark aesthetic.
Connect to your PC using its local IP address.
Select from available Ollama models.
Requirements
An Android device with NoiosoAI installed.
A PC running Windows, Linux, or macOS.
At least 8 GB of RAM recommended.
Around 20–30 GB of free storage recommended.
A CPU with 2–4 threads or more; a compatible GPU can improve performance.
Ollama.

Actual performance depends on your hardware and the model you choose.

Getting Started
1. Install Ollama

Download Ollama from ollama.com.

2. Download a model

Run the following command:

bash
ollama pull llama3.2:1b

3. Start the server

Linux / macOS:

bash
OLLAMA_HOST=0.0.0.0:11434 ollama serve


Windows PowerShell:

powershell
$env:OLLAMA_HOST="0.0.0.0:11434"
ollama serve

4. Connect your phone

Make sure your PC and Android phone are connected to the same network. Find your PC's local IP address, then enter it in NoiosoAI with port 11434.

Example:

text
http://10.1.1.1:11434


Select your downloaded model, such as llama3.2:1b, and start chatting.

Security: Only allow trusted devices to access your Ollama server. Avoid exposing port 11434 directly to the public internet.
Remote Access

To use NoiosoAI outside your home network, I recommend Tailscale instead of router port forwarding.

Install Tailscale on both devices and use your PC's Tailscale IP address with port 11434. Keep access restricted to trusted devices.

Tech Stack
Language: Kotlin
UI: Jetpack Compose
Networking: Retrofit and OkHttp
JSON: Moshi
Asynchronous Operations: Kotlin Coroutines and Flow
Storage: Jetpack DataStore
Screenshots

<p align="center">
<img width="280" alt="NoiosoAI Screenshot 1" src="https://github.com/user-attachments/assets/68b1cfd8-bf84-4814-93ab-a04b3ba3f814" />
<img width="280" alt="NoiosoAI Screenshot 2" src="https://github.com/user-attachments/assets/efe55026-7987-4cd6-bb6c-78dac0d9b369" />
</p>

AI-Assisted Development

NoiosoAI was developed with assistance from Gemini in Android Studio.

Roadmap
Connect to a local Ollama server.
Build the Android chat interface.
Add model selection.
Improve stability and fix bugs.
Develop Version 2.0.0.
Introduce more features and improvements.
Bug Reports

If you encounter an issue, check that Ollama is running, your server address is correct, and your firewall allows the connection.

If the problem persists, open an issue on GitHub.

Feedback and suggestions are welcome.

License

Distributed under the MIT License. See LICENSE for details.

Thanks for checking out NoiosoAI and supporting the project!
License

Distributed under the MIT License. See LICENSE for details.

Thanks for checking out NoiosoAI and supporting the project!
