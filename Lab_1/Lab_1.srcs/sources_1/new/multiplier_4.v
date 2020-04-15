`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 04/08/2020 06:51:54 PM
// Design Name: 
// Module Name: multiplier_4
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module multiplier_4(
    input clk,
    input res_n,
    input start,
    output reg done,
    input [15:0] arg1,
    input [15:0] arg2,
    output reg [31:0] product
    );
    
    reg [1:0] state;

    integer count;
        
    always @(posedge clk) begin
        case (state)
            2'b00:
                if (start) begin
                    product = 0;
                    count = arg2;
                    done = 1'b0;
                    state = 2'b01;
                end
            2'b01:
                if (count == 0) begin

                    done = 1'b1;
                    state = 2'b10;
                end
                else if(count >= 4)begin
                    product = product + arg1*4;
                    count = count - 4;
                    
                end
                else begin
                    product = product + arg1*count;
                    count = 0;
                    state = 2'b01;
                    done = 1'b1;
                end
            2'b10:
                if (!res_n) begin
                    state = 2'b00;
                end
                else begin 
                    done = 1'b1;
                end
            2'b11: state = 2'b00;
            default: state = 2'b00;
        endcase
        
    end 

endmodule
