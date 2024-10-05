package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"
)

func loadAPIKey() (string, error) {
	file, err := os.Open(".env")
	if err != nil {
		return "", err
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		line := scanner.Text()
		if strings.HasPrefix(line, "OPENAI_API_KEY=") {
			return strings.TrimPrefix(line, "OPENAI_API_KEY="), nil
		}
	}
	return "", nil
}

func handler(w http.ResponseWriter, r *http.Request) {
	apiKey, err := loadAPIKey()
	if err != nil || apiKey == "" {
		http.Error(w, "API Key not found", http.StatusInternalServerError)
		return
	}

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	requestBody, _ := json.Marshal(map[string]interface{}{
		"model": "gpt-3.5-turbo",
		"messages": []map[string]string{
			{"role": "user", "content": string(body)},
		},
	})

	req, err := http.NewRequest("POST", "https://api.openai.com/v1/chat/completions", bytes.NewBuffer(requestBody))
	if err != nil {
		http.Error(w, "Error creating request", http.StatusInternalServerError)
		return
	}

	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", "Bearer "+apiKey)

	client := &http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		http.Error(w, "Error contacting OpenAI API", http.StatusInternalServerError)
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		http.Error(w, "OpenAI API error", resp.StatusCode)
		return
	}

	var respBody map[string]interface{}
	err = json.NewDecoder(resp.Body).Decode(&respBody)
	if err != nil {
		http.Error(w, "Error decoding OpenAI response", http.StatusInternalServerError)
		return
	}

	choices := respBody["choices"].([]interface{})
	if len(choices) == 0 {
		http.Error(w, "No response from OpenAI", http.StatusInternalServerError)
		return
	}

	message := choices[0].(map[string]interface{})["message"].(map[string]interface{})["content"].(string)
	w.Write([]byte(message))
}

func main() {
	http.HandleFunc("/", handler)
	log.Println("Server started on port 8084")
	log.Fatal(http.ListenAndServe(":8084", nil))
}