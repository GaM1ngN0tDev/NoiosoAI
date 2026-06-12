# NoiosoAI 🌌

This App Is For Android And It Allows You To Connect To Your Ollama Server By Using The Local IP (example. 10.1.1.1) (This Is An Beta There Are Still Some Issues) (I Used The Gemini For Android Studio! Remeber This!)

## ✨ Features
NoiosoAI is a modern, privacy-focused Android chat application designed to connect to your local **Ollama** server. It features a beautiful "Material Expressive 3" UI with a living, animated background and a premium dark mode aesthetic.

### Requires:
- A PC With (Windows, Linux, MacOS)
- 8GB of RAM
- At least 20-30GB of Storage
- A CPU with 4 or 2 Threads (may use GPU even for faster speed)
- [Ollama](https://ollama.com/)

### 🚀 Getting Started
1. **Pull a Model**: I recommend using llama3.2:1b. Run: `ollama pull llama3.2:1b`
2. **Start Server**: Run `OLLAMA_HOST=0.0.0.0 ollama serve`
3. **Connect**: On your phone, put the local IP of your PC with `:11434` at the end and select the model name.

> [!TIP]
> **Remote Access**: If you want to use NoiosoAI from cellular data or outside your home, use **Tailscale** instead of port-forwarding. It's much more secure and provides a static IP for your PC.

Now you're ready to start chatting with your LLM locally!

## 🛠 Tech Stack
- **UI**: Jetpack Compose
- **Network**: Retrofit + OkHttp
- **JSON**: Moshi
- **Async**: Kotlin Coroutines & Flow
- **Data**: Jetpack DataStore

## 📸 Screenshots
<p align="center">
<img width="300" alt="Screenshot_1" src="https://github.com/user-attachments/assets/68b1cfd8-bf84-4814-93ab-a04b3ba3f814" />
<img width="300" alt="Screenshot_2" src="https://github.com/user-attachments/assets/efe55026-7987-4cd6-bb6c-78dac0d9b369" />
</p>

## 📄 License
Distributed under the MIT License. See `LICENSE` for more information.
