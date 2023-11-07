package uk.gov.nationalarchives.tre

import io.circe.syntax.EncoderOps
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity

class SlackUtils(client: HttpClient) {
  def postMessage(
     webhookUrl: String,
     message: String,
     channel: String,
     username: String
   ): Unit = {
    val slackMessage = Map(
      "channel" -> channel,
      "username" -> username,
      "text" -> message,
    )
    val post = new HttpPost(webhookUrl)
    post.setEntity(new StringEntity(slackMessage.asJson.toString()))
    val response = client.execute(post)
    println(Map("message" -> message, "status_code" -> response.getStatusLine.getStatusCode, "response" -> response.getEntity.getContent))
  }
}
