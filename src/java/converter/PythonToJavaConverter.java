package com.grkn.converter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;

public enum PythonToJavaConverter {
  INSTANCE;

  private final ObjectMapper objectMapper = new ObjectMapper();

  PythonToJavaConverter() {
  }

  public static PythonToJavaConverter getInstance() {
    return INSTANCE;
  }

  /**
   * Executes an external process and returns the output
   * @param command the command to execute
   * @return the output of the process
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public String executeProcess(String... command) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.redirectErrorStream(true);

    Process process = processBuilder.start();

    StringBuilder output = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }

    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException("Process exited with code: " + exitCode);
    }

    return output.toString().trim();
  }

  /**
   * Converts a JSON string to a JsonNode
   * @param jsonString the JSON string to convert
   * @return the JsonNode representation
   * @throws IOException if the string is not valid JSON
   */
  public JsonNode stringToJsonObject(String jsonString) throws IOException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      throw new IOException("JSON string cannot be null or empty");
    }
    return objectMapper.readTree(jsonString);
  }

  /**
   * Generic method to convert a JSON string to a DTO object
   * @param <T> the type of the DTO object
   * @param jsonString the JSON string to convert
   * @param dtoClass the class of the DTO object
   * @return the DTO object
   * @throws IOException if the string is not valid JSON or cannot be mapped to the DTO class
   */
  public <T> T stringToDto(String jsonString, Class<T> dtoClass) throws IOException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      throw new IOException("JSON string cannot be null or empty");
    }
    if (dtoClass == null) {
      throw new IOException("DTO class cannot be null");
    }
    return objectMapper.readValue(jsonString, dtoClass);
  }

  /**
   * Runs a Python script with arguments and returns the JSON output
   * @param scriptName the name of the Python script to run
   * @param args optional arguments to pass to the script
   * @return the JSON output from the script as a string
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public String run(String scriptName, String... args) throws IOException, InterruptedException {
    if (scriptName == null || scriptName.trim().isEmpty()) {
      throw new IOException("Script name cannot be null or empty");
    }

    String[] command = new String[args.length + 2];
    command[0] = "python";
    command[1] = scriptName;
    System.arraycopy(args, 0, command, 2, args.length);

    return executeProcess(command);
  }

  /**
   * Runs a Python script with arguments and returns the result as a DTO object
   * @param <T> the type of the DTO object
   * @param scriptName the name of the Python script to run
   * @param dtoClass the class of the DTO object
   * @param args optional arguments to pass to the script
   * @return the DTO object parsed from the script's JSON output
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the process is interrupted
   */
  public <T> T runAndParse(String scriptName, Class<T> dtoClass, String... args) throws IOException, InterruptedException {
    if (scriptName == null || scriptName.trim().isEmpty()) {
      throw new IOException("Script name cannot be null or empty");
    }
    if (dtoClass == null) {
      throw new IOException("DTO class cannot be null");
    }

    String jsonOutput = run(scriptName, args);
    return stringToDto(jsonOutput, dtoClass);
  }

  /**
   * Parses the result as a List of objects
   * @param <T> the type of objects in the list
   * @param jsonString the JSON array string
   * @param elementClass the class of the list elements
   * @return a list of objects
   * @throws IOException if parsing fails
   */
  public <T> List<T> parseList(String jsonString, Class<T> elementClass) throws IOException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      throw new IOException("JSON string cannot be null or empty");
    }
    return objectMapper.readValue(jsonString, objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
  }

  /**
   * Parses the result as a Map
   * @param jsonString the JSON object string
   * @return a map representation
   * @throws IOException if parsing fails
   */
  public Map<String, Object> parseMap(String jsonString) throws IOException {
    if (jsonString == null || jsonString.trim().isEmpty()) {
      throw new IOException("JSON string cannot be null or empty");
    }
    return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
  }

  /**
   * Wraps script execution result with status information
   * @param <T> the type of the data
   * @param scriptName the script name
   * @param dtoClass the class to parse data into
   * @param args script arguments
   * @return ExecutionResult containing status, data, and error information
   */
  public <T> ExecutionResult<T> runWithResult(String scriptName, Class<T> dtoClass, String... args) {
    ExecutionResult<T> result = new ExecutionResult<>();
    try {
      result.setData(runAndParse(scriptName, dtoClass, args));
      result.setStatus("success");
      result.setSuccess(true);
    } catch (InterruptedException e) {
      result.setStatus("interrupted");
      result.setError("Script execution was interrupted: " + e.getMessage());
      result.setSuccess(false);
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      result.setStatus("error");
      result.setError(e.getMessage());
      result.setSuccess(false);
    } catch (Exception e) {
      result.setStatus("unknown_error");
      result.setError("Unexpected error: " + e.getMessage());
      result.setSuccess(false);
    }
    return result;
  }

  /**
   * Wrapper class for standardized Python script execution results
   */
  public static class ExecutionResult<T> {
    private boolean success;
    private String status;
    private T data;
    private String error;

    public ExecutionResult() {
    }

    public ExecutionResult(boolean success, String status, T data, String error) {
      this.success = success;
      this.status = status;
      this.data = data;
      this.error = error;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public T getData() {
      return data;
    }

    public void setData(T data) {
      this.data = data;
    }

    public String getError() {
      return error;
    }

    public void setError(String error) {
      this.error = error;
    }

    @Override
    public String toString() {
      return "ExecutionResult{" +
          "success=" + success +
          ", status='" + status + '\'' +
          ", data=" + data +
          ", error='" + error + '\'' +
          '}';
    }
  }
}
