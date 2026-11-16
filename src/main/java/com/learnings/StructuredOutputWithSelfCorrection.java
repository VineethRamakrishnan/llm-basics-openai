package com.learnings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import com.learnings.constants.OpenAIModelList;
import com.learnings.model.Profile;

/**
 * This class to demonstrate the self correction by the model through 
 * updating the user message upon exception
 * 
 * @author Vineeth
 *
 */
public class StructuredOutputWithSelfCorrection {

	static OpenAIClient client = OpenAIOkHttpClient.fromEnv();
	
	public static void main(String[] args) {
	
		mapTheCandidateProfileWithSelfLearning();

	}
	
	private static void mapTheCandidateProfileWithSelfLearning() {
		
        ObjectMapper mapper = new ObjectMapper();
        Profile profile;
        String query1 = """
				I'm candidate applying for Tech lead position,
				I'm Vineeth from Chennai. I'm learnt man and having around 10 years of experience mainly in Backend development 
				involving Java, SpringBoot, Microservices tech.
				You can find my development work here https://github.com/VineethRamakrishnan
				""";
        ChatCompletionCreateParams.Builder params = ChatCompletionCreateParams.builder()
											.model(OpenAIModelList.MiniModel)
											.addSystemMessage("Read the user message and return the details as JSON.")
											.addUserMessage(query1);
        
        for(int attempt=1; attempt<=2; attempt++) {
        	
        	String reply = client.chat().completions().create(params.build())
                    .choices().get(0).message().content().orElse("");
        	
        	try {
				
        		profile = mapper.readValue(reply, Profile.class);
        		System.out.println("Attempt "+attempt+" worked..");
        		System.out.println(profile);
				
			} catch (JsonMappingException e) {
				System.out.println("Mapping exception");
				
				//Here, we're updating the user message to have the reply in correct format - LLM learning from the mistakes
				params
					.addAssistantMessage(reply)
					.addUserMessage("Your JSON could not be read: " + e.getOriginalMessage()
							+ ". Reply again with ONLY a JSON object with exactly these keys: "
							+ "name, gender, githubLink, experience, skillSet, location");			
			} catch (JsonProcessingException e) {
				System.out.println("Processing exception..");
				e.printStackTrace();
			}
        }
	}

}
