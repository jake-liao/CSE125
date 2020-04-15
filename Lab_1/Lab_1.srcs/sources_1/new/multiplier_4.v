/* 
CSE125 Lab 1 Multiplier_4.v
Professor Heiner Litz
Mark Zarkharov 
Jake Liao

Moore state machine
DEFAULT initializes PRODUCT, COUNT, and DONE to 0 then transitions to STATE 00
STATE 00 maintains PRODUCT, COUNT, and DONE at 0 until START transitions to STATE 01
STATE 01 accumulates PRODUCT and increments COUNT until COUNT == ARG2 transitioning to STATE 10
STATE 10 maintains DONE at 1 until !RES_N which transitions to STATE 00
STATE 11 is unused, immediately transition to STATE 00  

4x Speed
For as long as there are more than 4 iterations(COUNT) left in the accumulator, the accumulator will
increment by 4*arg1 every iteration. When there are less than 4 iterations left, the accumulator
will accumulate once by (arg2 - count) to complete the final iteration.
*/
`timescale 1ns / 1ps
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
                if (start) begin                // transition condition & change state
                    state = 2'b01;
                end
                else begin                      // state output
                    product = 32'b0;
                    count = 0;
                    done = 1'b0;
                    state = 2'b00;
                end
            2'b01:
                if (count == arg2) begin        // transition condition & change state
                    state = 2'b10;
                end
                else if(4 <= arg2 - count)begin  // ACCUMULATOR iff arg2 - count >= 4, accumulate at 4x speed
                    product = product + arg1*4;
                    count = count + 4;
                    state = 2'b01;
                end
                else begin                      // ACCUMULATOR iff 4 > count > 0, accumulate by arg2 - count once
                    product = product + arg1*(arg2 - count);
                    count = count + (arg2 - count);
                    state = 2'b01;
                end
            2'b10:
                if (!res_n) begin               // transition condition & change state
                    state = 2'b00;
                end
                else begin                      // state output
                    done = 1'b1;
                    state = 2'b10;
                end
            2'b11: state = 2'b00;               // unused state, transition to state 00
            default:                            // initialization and transition to state 00
                begin                       
                    product = 0;
                    count = 0;
                    done = 1'b0;
                    state = 2'b00;   
                end
        endcase
    end 
endmodule
