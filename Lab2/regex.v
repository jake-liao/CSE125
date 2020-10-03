/* 
 CSE125 Lab 2 regex.v
 Professor Heiner Litz
 Jake Liao
 Mark Zakharov
*/

`timescale 1ns / 1ps

module regex(
    input clk,
    input res_n,
    input [1:0] symbol_in,
    input last_symbol,
    output reg result,
    output reg done
    );
    
    reg [2:0] state, next_state;

    always @(*) begin
        casex({ state, symbol_in, last_symbol}) 
        	default: begin next_state = 3'b000; result = 1'b0; done = 1'b0; end
        		
        // STATE: S0
        	6'b000_00_0: begin next_state = 3'b001; result = 1'b0; done = 1'b0; end					
            6'b000_11_1: begin next_state = 3'b100; result = 1'b0; done = 1'b0; end				
            6'b000_01_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end				
            6'b000_10_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end				
            6'b000_11_0: begin next_state = 3'b101;	result = 1'b0; done = 1'b0; end				
            6'b000_00_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end				
            6'b000_01_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end				
            6'b000_10_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end				

        // STATE: S1
        	6'b001_01_0: begin next_state = 3'b001; result = 1'b0; done = 1'b0; end				
        	6'b001_10_0: begin next_state = 3'b010; result = 1'b0; done = 1'b0; end				
        	6'b001_00_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end				
        	6'b001_11_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end				
        	6'b001_xx_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end				

        // STATE: S2
        	6'b010_00_0: begin next_state = 3'b000; result = 1'b0; done = 1'b0; end		
        	6'b010_11_0: begin next_state = 3'b011; result = 1'b0; done = 1'b0; end		
        	6'b010_01_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end		
        	6'b010_10_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end		
        	6'b010_xx_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end		
        	
        // STATE: S3
        	6'b011_01_0: begin next_state = 3'b000; result = 1'b0; done = 1'b0; end		
        	6'b011_10_0: begin next_state = 3'b000; result = 1'b0; done = 1'b0; end		
        	6'b011_11_0: begin next_state = 3'b000; result = 1'b0; done = 1'b0; end		
        	6'b011_00_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end		
        	6'b011_xx_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end		

        // STATE: S4
            6'b100_xx_x: begin next_state = 3'b100; result = 1'b1; done = 1'b1; end

        // STATE: S5   		
            6'b101_xx_0: begin next_state = 3'b101; result = 1'b0; done = 1'b0; end		
        	6'b101_xx_1: begin next_state = 3'b110; result = 1'b0; done = 1'b0; end		

        // STATE: S6
            6'b110_xx_x: begin next_state = 3'b110; result = 1'b0; done = 1'b1; end

        // STATE: UNUSED
        	6'b111_xx_x: begin next_state = 3'b000; result = 1'b0; done = 1'b0; end		
        endcase
    end
    
    always @(posedge clk or negedge res_n) begin //registers
        if (~res_n) begin 
            state <= 3'b000;
        end 
        else begin
            state <= next_state;
        end
    end 

endmodule
