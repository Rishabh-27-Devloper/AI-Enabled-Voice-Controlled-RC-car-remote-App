package edu.remote;

import androidx.annotation.NonNull;
import okhttp3.*;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class GeminiApiService {
    private static final String API_KEY = "AIzaSyALXZu0ZwcKXDiWm9uPk6vJcgjWqa-1L-8";  // Replace with your actual key
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateText";
    private static final OkHttpClient client = new OkHttpClient();

    public interface ResponseCallback {
        void onSuccess(String response);
        void onFailure(String error);
    }
    public static void generateTextUsingSdk(String prompt, ResponseCallback callback) {
        try {
            // Build the generation configuration with JSON response MIME type.
            GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
            configBuilder.responseMimeType = "application/json";

            GenerationConfig generationConfig = configBuilder.build();

// Specify a Gemini model appropriate for your use case
            GenerativeModel gm =
                    new GenerativeModel(
                            /* modelName */ "gemini-1.5-flash",
                            // Access your API key as a Build Configuration variable (see "Set up your API key"
                            // above)
                            /* apiKey */ API_KEY,
                            /* generationConfig */ generationConfig);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            // Build the content with your prompt.
            Content content = new Content.Builder()
                    .addText(prompt)
                    .build();

            // Use a single-thread executor (adjust as needed)
            Executor executor = Executors.newCachedThreadPool();
            ListenableFuture<GenerateContentResponse> futureResponse = model.generateContent(content);

            Futures.addCallback(futureResponse, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String resultText = result.getText();
                    callback.onSuccess(resultText);
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onFailure("SDK Error: " + t.getMessage());
                }
            }, executor);
        } catch (Exception e) {
            callback.onFailure("SDK Exception: " + e.getMessage());
        }
    }
}