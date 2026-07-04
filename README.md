<div align="center">
<img width="1672" height="941" alt="b9fa9b7a-8f46-4202-91f3-59ba60472405" src="https://github.com/user-attachments/assets/389b9aa4-526a-47a5-8c9e-2c7a4f1f3eaf" />

</div>

# Run and deploy your AI Studio app
This project was developed by Google AI Studio Platform


## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
