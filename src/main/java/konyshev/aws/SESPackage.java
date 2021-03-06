package konyshev.aws;

import aws.S3SampleGet;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
/**
 * Created by ka on 13/09/15.
 */
public class SESPackage  {

    static final String FROM = "noreply@valgalla.org";  // Replace with your "From" address. This address must be verified.
    static final String TO = "konyshev.konstantin@gmail.com"; // Replace with a "To" address. If you have not yet requested
    // production access, this address must be verified.
    static final String BODY = "This email was sent through Amazon SES by using the AWS SDK for Java.";
    static final String SUBJECT = "Поступил новый заказ на авто";

    public static void cardorderevent(DynamodbEvent ddbEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        for (DynamodbEvent.DynamodbStreamRecord record : ddbEvent.getRecords()){
            logger.log(record.getEventID() + "\n");
            logger.log(record.getEventName() + "\n");
            logger.log(record.getDynamodb().toString() + "\n");

        }
        System.out.println("Successfully processed " + ddbEvent.getRecords().size() + " records.");
        carorder(context);
    }

    public static void carorder(Context context) {

        try {
            System.out.println("Attempting to send an email through Amazon SES by using the AWS SDK for Java...");

            /*
             * The ProfileCredentialsProvider will return your [default]
             * credential profile by reading from the credentials file located at
             * (~/.aws/credentials).
             *
             * TransferManager manages a pool of threads, so we create a
             * single instance and share it throughout our application.
             */
            AWSCredentials credentials = new AWSCredentials() {
                public String getAWSAccessKeyId() {
                    return "AKIAJNQMJFDEHTZGJNVQ";
                }
                public String getAWSSecretKey() {
                    return "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";
                }
            };


            String bucketName = "mobile-app-1";
            String key = "vehicle-services/order.tmpl";

            String template = S3SampleGet.getTmpl(credentials, bucketName, key);


            // Construct an object to contain the recipient address.
            Destination destination = new Destination().withToAddresses(new String[]{TO});

            // Create the subject and body of the message.
            Content subject = new Content().withData(SUBJECT);
            Content textBody = new Content().withData(template);
            Body body = new Body().withText(textBody);

            // Create a message with the specified subject and body.
            Message message = new Message().withSubject(subject).withBody(body);

            // Assemble the email.
            SendEmailRequest request = new SendEmailRequest().withSource(FROM).withDestination(destination).withMessage(message);



            // Instantiate an Amazon SES client, which will make the service call with the supplied AWS credentials.
            AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient(credentials);

            // Choose the AWS region of the Amazon SES endpoint you want to connect to. Note that your production
            // access status, sending limits, and Amazon SES identity-related settings are specific to a given
            // AWS region, so be sure to select an AWS region in which you set up Amazon SES. Here, we are using
            // the US East (N. Virginia) region. Examples of other regions that Amazon SES supports are US_WEST_2
            // and EU_WEST_1. For a complete list, see http://docs.aws.amazon.com/ses/latest/DeveloperGuide/regions.html
            Region REGION = Region.getRegion(Regions.US_WEST_2);
            client.setRegion(REGION);

            // Send the email.
            client.sendEmail(request);
            System.out.println("Email sent!");

        } catch (Exception ex) {
            System.out.println("The email was not sent.");
            System.out.println("Error message: " + ex.getMessage());
        }
    }
}
