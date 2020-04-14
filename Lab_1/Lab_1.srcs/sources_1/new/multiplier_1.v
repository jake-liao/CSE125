`timescale 1ns / 1ps

module multiplier_1(
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
                    count = 0;
                    done = 1'b0;
                    state = 2'b01;
                end
            2'b01:
                if (count == arg2) begin

                    done = 1'b1;
                    state = 2'b10;
                end
                else begin
                    product = product + arg1;
                    count = count + 1;
                    state = 2'b01;
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

