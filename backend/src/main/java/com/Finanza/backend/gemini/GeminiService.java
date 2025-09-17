package com.Finanza.backend.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.Finanza.backend.entities.Article;
import com.Finanza.backend.entities.Category;
import com.Finanza.backend.repositories.CategoryRepository;

@Service
public class GeminiService {
    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private static final String[] VALID_CATEGORIES = {"CRYPTO", "STOCKS", "FOREX", "COMMODITIES"};

    public ResponseEntity<?> summarize(SummarizeRequest request) {
        try {
            logRequest(request);
            String generatedText = generateArticle(request);
            return createResponse(generatedText);
        } catch (HttpClientErrorException e) {
            return handleHttpError(e);
        } catch (Exception e) {
            return handleGenericError(e);
        }
    }

    private void logRequest(SummarizeRequest request) {
        System.out.println("[GeminiService] Incoming summarize request: text=" + request.getText() +
                ", wordCount=" + request.getWordCount());
    }

    private String generateArticle(SummarizeRequest request) throws Exception {
        String prompt = buildPrompt(request);
        Client client = Client.builder().apiKey(apiKey).build();
        GenerateContentResponse response = client.models.generateContent("gemini-2.5-pro", prompt, null);
        String generatedText = response.text().trim();
        System.out.println("[GeminiService] Generated article: " + generatedText);
        return generatedText;
    }

    private String buildPrompt(SummarizeRequest request) {
        return String.format(
            "Sei un giornalista finanziario italiano esperto. " +
            "Scrivi un articolo professionale basato su questa richiesta: '%s'\n\n" +
            "ISTRUZIONI RIGOROSE:\n" +
            "1. Scrivi SOLO il titolo e il corpo dell'articolo\n" +
            "2. Includi una sezione 'Tags' con 2-3 hashtag rilevanti\n" +
            "3. NON usare markdown o formattazione speciale\n" +
            "4. NON aggiungere hashtag nel testo\n\n" +
            "STRUTTURA RICHIESTA:\n" +
            "**TITOLO DELL'ARTICOLO**\n\n" +
            "[Corpo dell'articolo di circa %d parole, diviso in 2-3 paragrafi. " +
            "Scrivi solo il contenuto dell'articolo senza tag, categorie e altre informazioni.]\n\n" +
            "Tags: #tag1 #tag2 #tag3\n" +
            "Category: [CRYPTO/STOCKS/FOREX/COMMODITIES]\n\n" +
            "IMPORTANTE: Il contenuto dell'articolo deve essere professionale e informativo, " +
            "senza hashtag o riferimenti ai tag nel testo principale.",
            request.getText(), 
            request.getWordCount()
        );
    }

    private ResponseEntity<?> createResponse(String generatedText) {
        try {
            // Estrai tutte le informazioni dal testo generato
            String title = extractTitle(generatedText);
            String body = extractBody(generatedText);
            List<String> tags = extractTagsFromText(generatedText);
            String categoryName = extractCategoryFromText(generatedText);

            // Log delle informazioni estratte
            System.out.println("[GeminiService] Extracted - Title: " + title);
            System.out.println("[GeminiService] Extracted - Body: " + body);
            System.out.println("[GeminiService] Extracted - Tags: " + tags);
            System.out.println("[GeminiService] Extracted - Category: " + categoryName);

            // Crea solo la risposta, non l'articolo qui
            SummarizeResponse response = new SummarizeResponse();
            response.setTitle(title);
            response.setBody(body);
            response.setTags(tags);
            response.setCategory(categoryName);
            response.setSummary(generatedText);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[GeminiService] Error in createResponse: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Errore durante la creazione della risposta: " + e.getMessage());
        }
    }

    private List<String> extractTagsFromText(String text) {
        List<String> tags = new ArrayList<>();
        
        // Cerca prima nella riga "Tags:"
        Pattern tagLinePattern = Pattern.compile("Tags:.*", Pattern.CASE_INSENSITIVE);
        Matcher tagLineMatcher = tagLinePattern.matcher(text);
        if (tagLineMatcher.find()) {
            String tagLine = tagLineMatcher.group();
            Pattern hashtagPattern = Pattern.compile("#(\\w+)");
            Matcher hashtagMatcher = hashtagPattern.matcher(tagLine);
            while (hashtagMatcher.find()) {
                String tag = hashtagMatcher.group(1);
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
        
        // Se non trova tag nella riga "Tags:", cerca hashtag nel testo
        if (tags.isEmpty()) {
            Pattern hashtagPattern = Pattern.compile("#(\\w+)");
            Matcher hashtagMatcher = hashtagPattern.matcher(text);
            while (hashtagMatcher.find()) {
                String tag = hashtagMatcher.group(1);
                if (!tags.contains(tag)) {
                    tags.add(tag);
                }
            }
        }
        
        return tags;
    }

    private String extractCategoryFromText(String text) {
        Pattern pattern = Pattern.compile("Category:\\s*(\\w+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String category = matcher.group(1).toUpperCase();
            for (String validCategory : VALID_CATEGORIES) {
                if (validCategory.equals(category)) {
                    return category;
                }
            }
        }
        return "STOCKS";
    }

    private String extractTitle(String text) {
        Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String extractBody(String text) {
        // Rimuove il titolo, i tag e la categoria, lasciando solo il corpo dell'articolo
        String body = text.replaceAll("\\*\\*.*?\\*\\*", "")  // rimuove il titolo
                         .replaceAll("Tags:.*", "")  // rimuove i tags e tutto quello che segue
                         .replaceAll("Category:.*", "")  // rimuove la categoria e tutto quello che segue
                         .replaceAll("#\\w+", "")  // rimuove eventuali hashtag rimasti
                         .replaceAll("\\n\\s*\\n", "\n")  // rimuove righe vuote multiple
                         .trim();
        
        // Rimuove eventuali righe che iniziano con "Tags:" o "Category:" ovunque nel testo
        String[] lines = body.split("\n");
        StringBuilder cleanBody = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("Tags:") && 
                !trimmedLine.startsWith("Category:") && 
                !trimmedLine.matches(".*#\\w+.*") && 
                !trimmedLine.isEmpty()) {
                cleanBody.append(line).append("\n");
            }
        }
        
        return cleanBody.toString().trim();
    }

    private ResponseEntity<?> handleHttpError(HttpClientErrorException e) {
        return ResponseEntity.status(e.getStatusCode())
                           .body(e.getResponseBodyAsString());
    }

    private ResponseEntity<?> handleGenericError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                           .body("Errore durante la generazione dell'articolo: " + e.getMessage());
    }
}