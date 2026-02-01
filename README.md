# Information Theory Compression Algorithms – Java Implementations

**Course Project | Information Theory**  
Zewail City of Science and Technology  

[![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 🎯 Project Overview

This repository contains modular Java implementations of key compression algorithms studied in an Information Theory course. The project demonstrates lossless and lossy techniques for text and image data, emphasizing entropy reduction, dictionary-based encoding, predictive coding, and quantization.

- **Lossless Algorithms**: LZ77, LZ78, Huffman, Adaptive Huffman – Focus on text/file compression with modular designs for encoding/decoding.
- **Lossy Algorithms**: DPCM (2D Feedback Predictive Coding) for grayscale images; Vector Quantization (VQ) for general vectors and images.
- **Image Compression Extension**: VQ applied to color images in RGB and YUV spaces, with comparisons across categories (nature, faces, animals) using metrics like PSNR, compression ratio, and visual quality.
- **Key Themes**: Modular architecture, performance optimization, real-time adaptability, and analysis of trade-offs (e.g., YUV vs RGB for better human visual perception).

All implementations are from scratch, with clean code, error handling, and outputs for metrics like compression ratio, MSE/PSNR, and file sizes.

## 🧬 Algorithms Implemented

### 1. LZ77 (Sliding-Window Dictionary Compression)
- **Description**: Identifies repeated sequences using a sliding window and encodes with backward references (offset, length).
- **PDF Report**: [Lz77.pdf](docs/Lz77.pdf)
- **Structure**: LZ77Main → FileHandler → LZ77Encoder → LZ77Decoder → CompressedFile
- **Usage**: Text/file compression; efficient for repetitive data.

### 2. LZ78 (Dynamic Dictionary Compression)
- **Description**: Builds a dictionary of patterns incrementally, encoding as index + next character.
- **PDF Report**: [Lz78.pdf](docs/Lz78.pdf)
- **Structure**: LZ78Main → FileHandler → LZ78Encoder → LZ78Decoder → CompressedFile
- **Usage**: Handles non-repetitive data; extensible for large files.

### 3. Huffman Coding (Static Entropy Encoding)
- **Description**: Builds a binary tree based on symbol frequencies for variable-length prefix codes.
- **PDF Report**: [huffman.pdf](docs/huffman.pdf)
- **Structure**: Main → FileHandler → HuffmanEncoder → HuffmanDecoder → CompressedFile
- **Usage**: Optimal for known frequency distributions; minimizes average code length.

### 4. Adaptive Huffman Coding (Dynamic Entropy Encoding)
- **Description**: Updates the Huffman tree dynamically during encoding/decoding, supporting static and adaptive modes with tree visualizations.
- **PDF Report**: [Adaptive huffman.pdf](docs/Adaptive%20huffman.pdf)
- **Structure**: Similar to Huffman, with added real-time tree adjustments.
- **Usage**: Ideal for streaming or unknown frequencies; eliminates pre-scan phase.

### 5. DPCM (2D Feedback Predictive Coding)
- **Description**: Lossy compression for grayscale images using predictors (Order-1, Order-2, Adaptive/JPEG-LS) and quantization levels (8/16/32).
- **PDF Report**: [DPCM .pdf](docs/DPCM%20.pdf)
- **Structure**: Modular with input validation; computes MSE, compression ratio, bit sizes.
- **Predictors**:
  - Order-1: Left pixel (A)
  - Order-2: A + B - C (top/diagonal)
  - Adaptive: Min/max rules
- **Usage**: Analyzes predictor/quantization impact on image quality.

### 6. Vector Quantization (VQ) with RGB/YUV Comparison
- **Description**: Lossy technique using codebook generation (e.g., LBG algorithm) for vector mapping; extended to images.
- **PDF Report**: [Vector Quatization (RGB-YUV).pdf](docs/Vector%20Quatization%20(RGB-YUV).pdf)
- **Structure**: Training/testing phases; separate modules for RGB and YUV.
- **Comparison**: Evaluated on nature/faces/animals images; YUV often superior (higher PSNR, better quality).
- **Usage**: Codebook sizes vary; metrics include distortion, compression ratio.

## 📈 Example Results (from Reports)

**Text Compression (LZ/Huffman)**:

| Algorithm         | Compression Ratio | Notes |
|-------------------|-------------------|-------|
| LZ77              | ~2.5:1            | Repetitive data |
| LZ78              | ~2.2:1            | Dynamic dict |
| Huffman           | ~1.8:1            | Static freq |
| Adaptive Huffman  | ~1.9:1            | Real-time adapt |

**Image DPCM** (Grayscale, varying quantization):

| Predictor | Quant Levels | MSE | Compression Ratio |
|-----------|--------------|-----|-------------------|
| Order-1   | 16           | Low | High              |
| Adaptive  | 32           | Mid | Balanced          |

**VQ Image Comparison** (Sample categories, codebook=256):

| Color Space | Category | PSNR (dB) | Compression Ratio |
|-------------|----------|-----------|-------------------|
| RGB         | Nature   | 28-30     | 8:1               |
| YUV         | Faces    | 32-35     | 8:1 (Better quality) |

## 🏆 Key Achievements

- Modular, maintainable code with separation of concerns.
- Visualizations (e.g., Huffman trees) and metrics for performance analysis.
- Comprehensive reports with test cases, methodology, and discussions.
- Demonstrates theory in practice: Entropy minimization, redundancy reduction, HVS exploitation in YUV.

