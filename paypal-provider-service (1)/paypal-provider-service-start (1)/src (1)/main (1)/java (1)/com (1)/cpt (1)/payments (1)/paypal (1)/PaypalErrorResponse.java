package com.cpt.payments.paypal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaypalErrorResponse {

	public PaypalErrorResponse(String string, String string2) {
		// TODO Auto-generated constructor stub
	}
	private String name;
	private String message;
	
	private String error;
	private String error_description;
}
