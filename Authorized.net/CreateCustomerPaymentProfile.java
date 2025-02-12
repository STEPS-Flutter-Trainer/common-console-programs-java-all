package connection;

import net.authorize.Environment;
import net.authorize.api.contract.v1.*;

import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.controller.CreateCustomerPaymentProfileController;
import net.authorize.api.controller.base.ApiOperationBase;

//author @krgupta
public class CreateCustomerPaymentProfile {
	
	public static String run(String apiLoginId, String transactionKey, String customerProfileId,String First_Name,String Last_Name,String Address_Line1,String email,String City,String State,String Pincode,String Country,String Phone_Number,String Credit_Card_Number,String Expiry_Date,String CVV) {

        ApiOperationBase.setEnvironment(Environment.SANDBOX);

        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType() ;
        merchantAuthenticationType.setName(apiLoginId);
        merchantAuthenticationType.setTransactionKey(transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);
		
	//private String getPaymentDetails(MerchantAuthenticationType merchantAuthentication, String customerprofileId, ValidationModeEnum validationMode) {
		CreateCustomerPaymentProfileRequest apiRequest = new CreateCustomerPaymentProfileRequest();
		apiRequest.setMerchantAuthentication(merchantAuthenticationType);
		apiRequest.setCustomerProfileId(customerProfileId);	

		//customer address
		CustomerAddressType customerAddress = new CustomerAddressType();
		customerAddress.setFirstName(First_Name);
		customerAddress.setLastName(Last_Name);
		customerAddress.setAddress(Address_Line1);
		customerAddress.setCity(City);
		customerAddress.setState(State);
		customerAddress.setZip(Pincode);
		customerAddress.setCountry(Country);
		customerAddress.setPhoneNumber(Phone_Number);

		//credit card details
		CreditCardType creditCard = new CreditCardType();
		creditCard.setCardNumber(Credit_Card_Number);
		creditCard.setExpirationDate(Expiry_Date);
		creditCard.setCardCode(CVV);

		CustomerPaymentProfileType profile = new CustomerPaymentProfileType();
		profile.setBillTo(customerAddress);

		PaymentType payment = new PaymentType();
		payment.setCreditCard(creditCard);
		profile.setPayment(payment);

		apiRequest.setPaymentProfile(profile);
		
		CreateCustomerPaymentProfileController controller = new CreateCustomerPaymentProfileController(apiRequest);
		controller.execute();
       
		CreateCustomerPaymentProfileResponse response = new CreateCustomerPaymentProfileResponse();
		response = controller.getApiResponse();
		if (response!=null) {
             if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
            	
                System.out.println(response.getCustomerPaymentProfileId());
 				System.out.println(response.getMessages().getMessage().get(0).getCode());
                System.out.println(response.getMessages().getMessage().get(0).getText());
                if (response.getValidationDirectResponse() != null)
                	System.out.println(response.getValidationDirectResponse());
            }
            else
            {
                System.out.println("Failed to create customer payment profile:  " + response.getMessages().getResultCode());
            }
        }

		return response.getCustomerPaymentProfileId();
	}	
}
