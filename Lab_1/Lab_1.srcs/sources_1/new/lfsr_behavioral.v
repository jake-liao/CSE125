`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 04/08/2020 06:56:39 PM
// Design Name: 
// Module Name: lfsr_behavioral
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


module lfsr_behavioral(
    input clk,
    input res_n,
    input [7:0] data_in,
    output reg [7:0] data_out
    );
    //testesetwtstestest
    always @(posedge clk or negedge res_n) begin //asynchronous reset active low FFs
        if(!res_n) //update input values of FFs in parallel
        begin
            data_out[0] <= data_in[0];
            data_out[1] <= data_in[1];
            data_out[2] <= data_in[2];
            data_out[3] <= data_in[3];
            data_out[4] <= data_in[4];
            data_out[5] <= data_in[5];
            data_out[6] <= data_in[6];
            data_out[7] <= data_in[7];
        end
        else //positive edge clock cycle shift
        begin
            data_out[0] <= data_out[7];
            data_out[1] <= data_out[0];
            data_out[2] <= data_out[1] ^ data_out[7];
            data_out[3] <= data_out[2] ^ data_out[7];
            data_out[4] <= data_out[3] ^ data_out[7];
            data_out[5] <= data_out[4];
            data_out[6] <= data_out[5];
            data_out[7] <= data_out[6];
        end
    end
    
endmodule
